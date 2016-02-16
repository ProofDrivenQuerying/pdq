package uk.ac.ox.cs.pdq.cost.estimators;

import static uk.ac.ox.cs.pdq.cost.CostStatKeys.COST_ESTIMATION_COUNT;
import static uk.ac.ox.cs.pdq.cost.CostStatKeys.COST_ESTIMATION_TIME;

import java.util.Collection;

import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.plan.AccessOperator;
import uk.ac.ox.cs.pdq.plan.DoubleCost;
import uk.ac.ox.cs.pdq.plan.Plan;


// TODO: Auto-generated Javadoc
/**
 * A simple cost estimator.
 * According to this implementation, the cost of a plan equals the number of the accesses.
 *
 * @author Efthymia Tsamoura
 * @param <P> the generic type
 */
public class AccessCountCostEstimator<P extends Plan> implements SimpleCostEstimator<P> {

	/** The stats. */
	protected final StatisticsCollector stats;
	
	/**
	 * Default constructor. By-passed any statistic collection.
	 */
	public AccessCountCostEstimator() {
		this(null);
	}


	/**
	 * Constructor.
	 *
	 * @param stats the stats
	 */
	public AccessCountCostEstimator(StatisticsCollector stats) {
		this.stats = stats;
	}

	/**
	 * Clone.
	 *
	 * @return SimpleCountCostEstimator<P,S>
	 * @see uk.ac.ox.cs.pdq.cost.estimators.SimpleCostEstimator#clone()
	 */
	@Override
	public AccessCountCostEstimator<P> clone() {
		return (AccessCountCostEstimator<P>) (this.stats == null ? new AccessCountCostEstimator<>(null) : new AccessCountCostEstimator<>(this.stats.clone()));
	}

	/**
	 * Cost.
	 *
	 * @param plan P
	 * @return DoubleCost
	 * @see uk.ac.ox.cs.pdq.cost.estimators.CostEstimator#cost(P)
	 */
	@Override
	public DoubleCost cost(P plan) {
		DoubleCost result = this.cost(plan.getAccesses());
		plan.setCost(result);
		return result;
	}

	/**
	 * Estimate cost.
	 *
	 * @param plan P
	 * @return Cost
	 * @see uk.ac.ox.cs.pdq.cost.estimators.SimpleCostEstimator#estimateCost(P)
	 */
	@Override
	public DoubleCost estimateCost(P plan) {
		return this.cost(plan.getAccesses());
	}

	/**
	 * Cost.
	 *
	 * @param accesses Collection<AccessOperator>
	 * @return DoubleCost
	 * @see uk.ac.ox.cs.pdq.costs.SimpleCostEstimator#cost(Collection<AccessOperator>)
	 */
	@Override
	public DoubleCost cost(Collection<AccessOperator> accesses) {
		if(this.stats != null){this.stats.start(COST_ESTIMATION_TIME);}
		DoubleCost result = new DoubleCost(accesses.size());
		if(this.stats != null){this.stats.stop(COST_ESTIMATION_TIME);}
		if(this.stats != null){this.stats.increase(COST_ESTIMATION_COUNT, 1);}
		return result;
	}

}
