package uk.ac.ox.cs.pdq.services.logicblox;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.plan.Cost;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.rewrite.RewriterException;
import uk.ac.ox.cs.pdq.services.MessageHandler;
import uk.ac.ox.cs.pdq.services.ServiceException;
import uk.ac.ox.cs.pdq.services.logicblox.cost.LogicBloxDelegateCostEstimator;
import uk.ac.ox.cs.pdq.services.logicblox.rewrite.DAGPlanToConjunctiveQuery;
import uk.ac.ox.cs.pdq.services.logicblox.rewrite.ProtoBufferUnwrapper;
import uk.ac.ox.cs.pdq.services.logicblox.rewrite.ProtoBufferUnwrapper.ParserException;
import uk.ac.ox.cs.pdq.services.logicblox.rewrite.PullEqualityRewriter;
import uk.ac.ox.cs.pdq.services.logicblox.rewrite.PushEqualityRewriter;
import uk.ac.ox.cs.pdq.services.logicblox.rewrite.QueryToProtoBuffer;
import uk.ac.ox.cs.pdq.services.util.BestRewritingPrinter;

import com.google.protobuf.GeneratedMessage;
import com.logicblox.common.protocol.CommonProto.Rule;
import com.logicblox.connect.BloxCommand.CommandResponse;
import com.logicblox.connect.BloxCommand.ExternalRuleOptimization;
import com.logicblox.connect.BloxCommand.ExternalRuleOptimizationResponse;
import com.logicblox.connect.BloxCommand.ExternalRuleOptimizationResponse.Status;

/**
 *  Handles optimization requests, and cost information messages between PDQ and LB.
 * 
 * @author Julien LEBLAY
 */
public class OptimizationHandler implements MessageHandler<ExternalRuleOptimization> {

	/** Logger. */
	static final Logger log = Logger.getLogger(OptimizationHandler.class);

	/** The input stream. */
	private final InputStream in;
	
	/** The output stream. */
	private final OutputStream out;
	
	/** The master service. */
	private final SemanticOptimizationService master;

	/**
	 * Instantiates a new optimization handler.
	 *
	 * @param master the master
	 * @param socket the socket
	 */
	public OptimizationHandler(SemanticOptimizationService master, Socket socket) {
		this.master = master;
		try {
			this.in = new BufferedInputStream(socket.getInputStream());
			this.out = new BufferedOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Handles the LB message, called "command", which is a BloxCommand object defined in the external LB library.
	 *  As soon as a 
	 *  request comes it creates a ProtoBufferUnwrapper object for the schema associated with the incoming message's workspace. Then:
	 *		(1) It uses the ProtoBufferUnwrapper object to transform the LB "rule" in the message to a PDQ ConjunctiveQuery, 
	 *		(2) it instantiates a LogicBloxDelegateCostEstimator, which in turn asks LB to cost the original "non-optimized" rule
     *  	(3) it calls the planner (passing the LB cost estimator above as a parameter) to optimize the rule and return a DAGPlan
	 *		(4) If the plan return has lower cost than the original query it uses the DAGPlanToConjunctiveQuery object to create a
	 *		    ConjunctiveQuery, 
	 *		    (4.1) It uses a QueryToProtoBuffer object (which is a Rewriter) to write the query into a google protobuf Rule object,
	 *		    and uses the external LB lib to transform this into a google protobuf message (a BloxCommand) delivering it to 
	 *		    SemanticOptimizationService which sends it to LB.
	 * @throws SQLException 
	 */
	@Override
	public GeneratedMessage handle(ExternalRuleOptimization command) {
		Rule result;
		try {
			log.debug("Optimization on workspace " + command.getWorkspace());
			Context context = this.master.resolve(command.getWorkspace());
			Schema schema = context.getSchema();
			
			// We initialize the ProtoBufferUnwrapper with a schema
			// but we later change the schema.
			// This OK because the only changes to the schema are access restrictions, while
			// the only use of the schema by the ProtoBufferUnwrapper is to resolve LB objects while translating them to PDQ.
			//
			// Still this looks ugly since the schema in the line below should necessarily come from the context 8 lines below.
			final ProtoBufferUnwrapper protoToCQ = new ProtoBufferUnwrapper(schema);
			ConjunctiveQuery query = protoToCQ.ruleToQuery(command.getRule());
			
			// modifies the access of the relations in the schema (and builds it again)
			// such that predicates preceeding the query's body predicates in the 
			// execution graph's topological sort orders are made inaccessible, while
			// all other relations have free access.
			schema = context.setAccesses(query);
			
			log.info("Optimizing:" + query);
			// ???
			// Julien's comment:
			// Here, we do not rewrite directly into 'query' as the cost
			// estimator requires to share the canonical mapping of the 
			// query used by the planner.
			ConjunctiveQuery queryEq = query
						.rewrite(new PushEqualityRewriter<ConjunctiveQuery>());
			final LogicBloxDelegateCostEstimator<?> estimator =
					new LogicBloxDelegateCostEstimator<>(
							     schema, queryEq, this.in, this.out);
			Cost nonOptimizedCost = estimator.estimateCost(query);
			query = queryEq;
			DAGPlan optimized;
			try {
				optimized = this.optimize(schema, query, estimator,
						new BestRewritingPrinter());
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			if (optimized != null 
					&& optimized.getCost().lessThan(nonOptimizedCost)) {
				query = optimized
					.rewrite(new DAGPlanToConjunctiveQuery(query.getHead()))
					.rewrite(new PullEqualityRewriter<ConjunctiveQuery>(schema));
				log.info("Optimized to: " + optimized.getCost() + " - " + query);
				result = query.rewrite(new QueryToProtoBuffer(schema));
				return CommandResponse.newBuilder()
				    .setOptimizedRule(ExternalRuleOptimizationResponse
				    		.newBuilder().setRewriting(result)
				    		.setStatus(Status.DONE).build()).build();
			}
			result = command.getRule();
			log.info("Not optimized.");
			if (optimized != null) {
				log.info(optimized.getCost() + " vs. " + nonOptimizedCost);
			}
		} catch (RewriterException | ParserException e) {
			log.warn("Optimization aborted: " + e.getMessage());
			result = command.getRule();
		}
		return CommandResponse.newBuilder()
				.setOptimizedRule(ExternalRuleOptimizationResponse.newBuilder()
						.setStatus(Status.ABORTED).build()).build();
	}
	
	/**
	 * Runs the actual optimization by calling PDQ.
	 *
	 * @param schema the schema
	 * @param query Query<?>
	 * @param estimator CostEstimator<?>
	 * @param handlers EventHandler[]
	 * @return the optimized query, if any, null otherwise.
	 * @throws SQLException 
	 */
	private DAGPlan optimize(Schema schema, ConjunctiveQuery query, 
			CostEstimator<?> estimator, EventHandler... handlers) throws SQLException {
		log.debug("Optimizing query :" + query);
		
		PlannerParameters plannerParams = new PlannerParameters();
		CostParameters costParams = new CostParameters();
		ReasoningParameters reasoningParams = new ReasoningParameters();
		DatabaseParameters dbParams = new DatabaseParameters();
		
		try {
			ExplorationSetUp planner = new ExplorationSetUp(plannerParams, costParams, reasoningParams, dbParams, schema);
			planner.setCostEstimator(estimator);
			for (EventHandler eh: handlers) {
				planner.registerEventHandler(eh);
			}
			DAGPlan result = planner.search(query);
			log.debug("Best plan:" + result);
			return result;
		} catch (PlannerException e) {
			log.error(e);
		}
		log.info("Optimization complete.");
		return null;
	}
}
