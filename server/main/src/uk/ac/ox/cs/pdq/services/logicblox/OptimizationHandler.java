package uk.ac.ox.cs.pdq.services.logicblox;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
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

// TODO: Auto-generated Javadoc
/**
 * Handles commands coming from client (typically optimization requests
 * and cost estimates).
 * 
 * @author Julien LEBLAY
 */
public class OptimizationHandler implements MessageHandler<ExternalRuleOptimization> {

	/** Logger. */
	static final Logger log = Logger.getLogger(OptimizationHandler.class);

	/** The in. */
	private final InputStream in;
	
	/** The out. */
	private final OutputStream out;
	
	/** The master. */
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
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.services.MessageHandler#handle(com.google.protobuf.GeneratedMessage)
	 */
	@Override
	public GeneratedMessage handle(ExternalRuleOptimization command) {
		Rule result;
		try {
			log.debug("Optimization on workspace " + command.getWorkspace());
			Context context = this.master.resolve(command.getWorkspace());
			Schema schema = context.getSchema();
			final ProtoBufferUnwrapper protoToCQ = new ProtoBufferUnwrapper(schema);
			ConjunctiveQuery query = protoToCQ.ruleToQuery(command.getRule());
			
			// Make relations/rules that preceed the queries, predicate in the
			// execution inaccessible.
			schema = context.setAccesses(query);
			
			log.info("Optimizing:" + query);
			// Here, we do not rewrite directly into 'query' as the cost
			// estimator require to share the canonical mapping of the 
			// query used by the planner.
			ConjunctiveQuery queryEq = query
						.rewrite(new PushEqualityRewriter<ConjunctiveQuery>());
			final LogicBloxDelegateCostEstimator<?> estimator =
					new LogicBloxDelegateCostEstimator<>(
							     schema, queryEq, this.in, this.out);
			Cost nonOptimizedCost = estimator.estimateCost(query);
			query = queryEq;
			DAGPlan optimized = this.optimize(schema, query, estimator,
					new BestRewritingPrinter());
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
	 */
	private DAGPlan optimize(Schema schema, ConjunctiveQuery query, 
			CostEstimator<?> estimator, EventHandler... handlers) {
		log.debug("Optimizing query :" + query);
		
		PlannerParameters plannerParams = new PlannerParameters();
		CostParameters costParams = new CostParameters();
		ReasoningParameters reasoningParams = new ReasoningParameters();
		
		try {
			ExplorationSetUp planner = new ExplorationSetUp(plannerParams, costParams, reasoningParams, schema);
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
