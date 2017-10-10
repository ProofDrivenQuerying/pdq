package uk.ac.ox.cs.pdq.cost.estimators;

import static uk.ac.ox.cs.pdq.cost.logging.CostStatKeys.COST_ESTIMATION_COUNT;
import static uk.ac.ox.cs.pdq.cost.logging.CostStatKeys.COST_ESTIMATION_TIME;

import java.util.Collection;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.AlgebraUtilities;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.logging.StatisticsCollector;


/**
 * A simple cost estimator.
 * According to this implementation, the cost of a plan equals the number of the accesses.
 *
 * @author Efthymia Tsamoura
 */
public class CountNumberOfAccessedRelationsCostEstimator implements OrderIndependentCostEstimator {

	/** The stats. */
	protected final StatisticsCollector stats;

	/**
	 * Constructor.
	 *
	 * @param stats the stats
	 */
	public CountNumberOfAccessedRelationsCostEstimator(StatisticsCollector stats) {
		this.stats = stats;
	}

	/**
	 * Clone.
	 *
	 * @return SimpleCountCostEstimator<P,S>
	 * @see uk.ac.ox.cs.pdq.cost.estimators.OrderIndependentCostEstimator#clone()
	 */
	@Override
	public CountNumberOfAccessedRelationsCostEstimator clone() {
		return (CountNumberOfAccessedRelationsCostEstimator) (this.stats == null ? new CountNumberOfAccessedRelationsCostEstimator(null) : new CountNumberOfAccessedRelationsCostEstimator(this.stats.clone()));
	}

	/**
	 * Cost.
	 *
	 * @param plan P
	 * @return DoubleCost
	 * @see uk.ac.ox.cs.pdq.cost.estimators.CostEstimator#cost(P)
	 */
	@Override
	public DoubleCost cost(RelationalTerm plan) {
		DoubleCost result = this.cost(AlgebraUtilities.getAccesses(plan));
		return result;
	}

	/**
	 * Cost.
	 *
	 * @param accesses Collection<AccessOperator>
	 * @return DoubleCost
	 * @see uk.ac.ox.cs.pdq.costs.SimpleCostEstimator#cost(Collection<AccessOperator>)
	 */
	@Override
	public DoubleCost cost(Collection<AccessTerm> accesses) {
		if(this.stats != null){this.stats.start(COST_ESTIMATION_TIME);}
		DoubleCost result = new DoubleCost(accesses.size());
		if(this.stats != null){this.stats.stop(COST_ESTIMATION_TIME);}
		if(this.stats != null){this.stats.increase(COST_ESTIMATION_COUNT, 1);}
		return result;
	}
}
