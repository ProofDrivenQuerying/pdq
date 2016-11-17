package uk.ac.ox.cs.pdq.services.logicblox.cost;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.cost.estimators.BlackBoxCostEstimator;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.plan.Cost;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.plan.DoubleCost;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState;
import uk.ac.ox.cs.pdq.rewrite.RewriterException;
import uk.ac.ox.cs.pdq.services.logicblox.DelimitedMessageProtocol;
import uk.ac.ox.cs.pdq.services.logicblox.rewrite.DAGPlanToConjunctiveQuery;
import uk.ac.ox.cs.pdq.services.logicblox.rewrite.Deskolemizer;
import uk.ac.ox.cs.pdq.services.logicblox.rewrite.PullEqualityRewriter;
import uk.ac.ox.cs.pdq.services.logicblox.rewrite.QueryHeadProjector;
import uk.ac.ox.cs.pdq.services.logicblox.rewrite.QueryToProtoBuffer;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.logicblox.connect.BloxCommand.Command;
import com.logicblox.connect.BloxCommand.CommandResponse;
import com.logicblox.connect.BloxCommand.ExternalRuleOptimizationResponse;
import com.logicblox.connect.BloxCommand.ExternalRuleOptimizationResponse.Status;
import com.logicblox.connect.BloxCommand.RuleCostEstimate;
import com.logicblox.connect.ProtoBufException.ExceptionContainer;

/**
 * The Class LogicBloxDelegateCostEstimator. Interacts with the Logicblox server to determine the cost of the given rule.
 *
 * @param <S> the generic type
 */
public class LogicBloxDelegateCostEstimator<S extends AccessibleChaseState>
		implements BlackBoxCostEstimator<DAGPlan> {

	/** Logger. */
	static final Logger log = Logger.getLogger(LogicBloxDelegateCostEstimator.class);

	/** The input stream. */
	private final InputStream in;
	
	/** The output stream. */
	private final OutputStream out;
	
	/** The schema registered so far. */
	private final Schema schema;
	
	/** The conjunctive rule to cost. */
	private final ConjunctiveQuery rule;
	
	/**
	 * Default constructor. Ignores statistic collection.
	 *
	 * @param schema Schema
	 * @param query the query
	 * @param in InputStream
	 * @param out OutputStream
	 */
	public LogicBloxDelegateCostEstimator(Schema schema, ConjunctiveQuery rule, InputStream in, OutputStream out) {
		super();
		this.schema = schema;
		this.rule = rule;
		this.in = in;
		this.out = out;
	}

	/**
	 * Method clone.
	 * @return LogicBloxDelegateCostEstimator<S>
	 * @see uk.ac.ox.cs.pdq.costs.CostEstimator#clone()
	 */
	@Override
	public LogicBloxDelegateCostEstimator<S> clone() {
	    return new LogicBloxDelegateCostEstimator<>(this.schema, this.rule, this.in, this.out);
	}

	/**
	 * Checks if the rule is recursive.
	 *
	 * @param q the q
	 * @return true, if is recursive
	 */
	private boolean isRecursive(ConjunctiveQuery q) {
		for (Atom pred : q.getAtoms()) {
			if (this.rule.getHead().getPredicate().equals(pred.getPredicate())
					&& this.rule.getHead().getSchemaConstants().equals(pred.getSchemaConstants())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Interacts with the Logicblox server to determine the cost of the given rule.
	 * If the rule is recursive, and the deafult upper bound cost is returned. 
	 * Else the following happen:
	 * 	(1) it uses the QueryToProtoBufferRewriter to rewrite the input ConjunctiveQuery object to an LB/google-protobuf Rule object 
	 * 	and through calls to the LB linked lib this gets transformed to a (externally defined) CommandResponse object, 
	 * 	which models the respond to a command/message.
	 * 	(2) It then uses the DelimitedMessageProtocol.java to send the message to LB (if something goes while using the 
	 * 	QueryToProtoBufferRewriter wrong it sends an exception message to LB). 
	 *  (3) It then receives a message, through DelimitedMessageProtocol, which is expected to be an LB/google-protobuf (externally
	 *  defined) "Option" object.
	 *  (4) If Option.isSome() is false it means that we received a proble LB message that checks whether PDQ is alive, and we ignore it.
	 *  If Option.isSome() is true it means we can extract a RuleCostEstimate LB/google-protobuf (externally defined) object.
	 *  On this we can get a cost, which this estimator does, and returns it.
	 *
	 * @param r ConjunctiveQuery
	 * @return the cost of the input query as estimated by LogicBlox.
	 */
	public Cost estimateCost(ConjunctiveQuery r) {
		if (this.isRecursive(r)) {
			return DoubleCost.UPPER_BOUND;
		}
		try {
			try {
				CommandResponse response = CommandResponse.newBuilder()
						.setOptimizedRule(
							ExternalRuleOptimizationResponse.newBuilder()
								.setRewriting(r.rewrite(new QueryToProtoBuffer(this.schema)))
							.setStatus(Status.COST_INFO).build())
							.build();
				DelimitedMessageProtocol.send(this.out, response);
			} catch (final InvalidProtocolBufferException exc) {
				// format error: send message error back
				final CommandResponse.Builder builder = CommandResponse.newBuilder();
						builder.setException(ExceptionContainer.newBuilder()
								.setMessage(exc.getMessage()).build());
				CommandResponse response = builder.build();
				DelimitedMessageProtocol.send(this.out, response);
				exc.printStackTrace();
				throw new IllegalStateException(exc);
			}
			com.logicblox.common.Option<? extends GeneratedMessage> optRequest = com.logicblox.common.Option.none();
			try {
				optRequest = DelimitedMessageProtocol.receive(this.in);
				if (optRequest.isSome()) {
					final GeneratedMessage req = optRequest.unwrap();
					if (req instanceof Command) {
						RuleCostEstimate estimate = ((Command) req).getRuleToCost();
						log.debug("\tCost: " + estimate.getCost() + " = " + r);
						return new DoubleCost(estimate.getCost());
					}
				}
//				throw new RuntimeException(new PlannerException("Communication failure with LB"));
			} catch (final InvalidProtocolBufferException exc) {
				// format error: send message error back
				final CommandResponse.Builder builder = CommandResponse.newBuilder();
					builder.setException(ExceptionContainer.newBuilder()
						.setMessage(exc.getMessage()).build());
				CommandResponse response = builder.build();
				DelimitedMessageProtocol.send(this.out, response);
				throw new IllegalStateException(exc);
			}
		} catch (IOException | RewriterException e) {
			throw new IllegalStateException(e);
		}
		return DoubleCost.UPPER_BOUND;
	}

	/**
	 * ??? This seems as an infinite loop.
	 * Converts the input plan to a conjunctive query amenable to cost 
	 * estimation by LogicBlox.
	 *
	 * @param p the p
	 * @return the cost of the input plan as estimated by LogicBlox.
	 */
	@Override
	public Cost cost(DAGPlan p) {
		Cost result = this.cost(p);
		p.setCost(result);
		return result;
	}

	/**
	 * Converts the input plan to a conjunctive query amenable to cost 
	 * estimation by LogicBlox.
	 * @param plan DAGPlan
	 * @return the cost of the input plan as estimated by LogicBlox.
	 */
	@Override
	public Cost estimateCost(DAGPlan plan) {
		try {
			ConjunctiveQuery q = plan
					.rewrite(new DAGPlanToConjunctiveQuery())
					.rewrite(new QueryHeadProjector(this.rule))
					.rewrite(new Deskolemizer<ConjunctiveQuery>())
					.rewrite(new PullEqualityRewriter<ConjunctiveQuery>(this.schema))
					;
			return this.estimateCost(q);
		} catch (RewriterException e) {
			throw new IllegalStateException(e);
		}
	}
}
