package uk.ac.ox.cs.pdq.cost.estimators;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.plan.AccessOperator;
import uk.ac.ox.cs.pdq.plan.DoubleCost;
import uk.ac.ox.cs.pdq.plan.Plan;

/**
 * Cost estimator favoring query with more atoms.
 *
 * @author Julien Leblay
 */
public class LengthBasedCostEstimator<P extends Plan> implements BlackBoxCostEstimator<P> {

	protected final StatisticsCollector stats;
	
	/**
	 * Default constructor. Ignores statistics collection.
	 */
	public LengthBasedCostEstimator() {
		this(null);
	}
	
	/**
	 * Constructor
	 * @param stats
	 */
	public LengthBasedCostEstimator(StatisticsCollector stats) {
		this.stats = stats;
	}

	/**
	 * @return LengthBasedCostEstimator<P>
	 * @see uk.ac.ox.cs.pdq.plan.cost.CostEstimator#clone()
	 */
	@Override
	public LengthBasedCostEstimator<P> clone() {
		return (LengthBasedCostEstimator<P>) (this.stats == null ? new LengthBasedCostEstimator<>(null) : new LengthBasedCostEstimator<>(this.stats.clone()));
	}

	/**
	 * @param plan P
	 * @return Cost
	 * @see uk.ac.ox.cs.pdq.plan.cost.CostEstimator#cost(P)
	 */
	@Override
	public DoubleCost cost(P plan) {
		DoubleCost result = this.estimateCost(plan);
		plan.setCost(result);
		return result;
	}

	/**
	 * @param plan P
	 * @return Cost
	 * @see uk.ac.ox.cs.pdq.plan.cost.CostEstimator#estimateCost(P)
	 */
	@Override
	public DoubleCost estimateCost(P plan) {
		List<AccessOperator> accesses = new ArrayList<>();
		for (AccessOperator access: plan.getAccesses()) {
			if (!accesses.contains(access)) {
				accesses.add(access);
			}
		}
		DoubleCost result = new DoubleCost(1.0 / accesses.size());
		return result;
	}
}
