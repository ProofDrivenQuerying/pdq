package uk.ac.ox.cs.pdq.services.logicblox.cost;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.cost.estimators.BlackBoxCostEstimator;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.plan.Cost;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.plan.DoubleCost;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;
import uk.ac.ox.cs.pdq.rewrite.RewriterException;
import uk.ac.ox.cs.pdq.services.logicblox.DelimitedMessageProtocol;
import uk.ac.ox.cs.pdq.services.logicblox.rewrite.DAGPlanToConjunctiveQuery;
import uk.ac.ox.cs.pdq.services.logicblox.rewrite.Deskolemizer;
import uk.ac.ox.cs.pdq.services.logicblox.rewrite.PullEqualityRewriter;
import uk.ac.ox.cs.pdq.services.logicblox.rewrite.QueryHeadProjecor;
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
 */
public class LogicBloxDelegateCostEstimator<S extends AccessibleChaseState>
		implements BlackBoxCostEstimator<DAGPlan> {

	/** Logger. */
	static final Logger log = Logger.getLogger(LogicBloxDelegateCostEstimator.class);

	private final InputStream in;
	private final OutputStream out;
	private final Schema schema;
	private final ConjunctiveQuery query;
	
	/**
	 * Default constructor. Ignores statistic collection.
	 * @param schema Schema
	 * @param in InputStream
	 * @param out OutputStream
	 */
	public LogicBloxDelegateCostEstimator(Schema schema, ConjunctiveQuery query, InputStream in, OutputStream out) {
		super();
		this.schema = schema;
		this.query = query;
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
	    return new LogicBloxDelegateCostEstimator<>(this.schema, this.query, this.in, this.out);
	}

	private boolean isRecursive(ConjunctiveQuery q) {
		for (Predicate pred : q.getBody().getPredicates()) {
			if (this.query.getHead().getSignature().equals(pred.getSignature())
					&& this.query.getHead().getSchemaConstants().equals(pred.getSchemaConstants())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Interacts with the Logicblox server to determine the cost of the given
	 * query.
	 * @param q ConjunctiveQuery
	 * @return the cost of the input query has estimation by LogicBlox.
	 */
	public Cost estimateCost(ConjunctiveQuery q) {
		if (this.isRecursive(q)) {
			return DoubleCost.UPPER_BOUND;
		}
		try {
			try {
				CommandResponse response = CommandResponse.newBuilder()
						.setOptimizedRule(
							ExternalRuleOptimizationResponse.newBuilder()
								.setRewriting(q.rewrite(new QueryToProtoBuffer(this.schema)))
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
						log.debug("\tCost: " + estimate.getCost() + " = " + q);
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
	 * Converts the input plan to a conjunctive query amenable to cost 
	 * estimation by LogicBlox.
	 * @param plan DAGPlan
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
					.rewrite(new QueryHeadProjecor(this.query))
					.rewrite(new Deskolemizer<ConjunctiveQuery>())
					.rewrite(new PullEqualityRewriter<ConjunctiveQuery>(this.schema))
					;
			return this.estimateCost(q);
		} catch (RewriterException e) {
			throw new IllegalStateException(e);
		}
	}
}
