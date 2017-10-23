package uk.ac.ox.cs.pdq.planner.dominance;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.estimators.OrderIndependentCostEstimator;

/**
* Closed success dominance. 
* A closed plan p success dominates another closed plan p', if p is successful and has cost < the cost of p'.
*
* @author Efthymia Tsamoura
*/

/**
* Open success dominance.
* A plan p success dominates another closed plan ', if p is successful and has cost < the cost of p'.
* If either of the plans is open, then a simple plan cost estimator is used to assess their cost;
* otherwise, the costs of their corresponding (closed) plans are considered.
*
* @author Efthymia Tsamoura
*/

public class SuccessDominance {

	//	/**  True if we use a simple cost function to compare plans. */
	//	private final boolean simpleFunction;

	/** The estimator. */
	private final OrderIndependentCostEstimator costEstimatorForOpenPlans;

	/**
	 * Constructor for SuccessDominance.
	 * @param simpleFunction Boolean
	 */
	public SuccessDominance(OrderIndependentCostEstimator estimator) {
		Preconditions.checkNotNull(estimator);
		this.costEstimatorForOpenPlans = estimator;
	}

	/**
	 * Checks if is dominated.
	 *
	 * @param source the source
	 * @param target the target
	 * @return true if the source plan is success dominated by the target
	 */
	public boolean isDominated(RelationalTerm source, Cost sourceCost, RelationalTerm target, Cost targetCost) {
		if(source.isClosed() && target.isClosed()) 
			return sourceCost.greaterThan(targetCost);
		
		return this.costEstimatorForOpenPlans.cost(source).greaterThan(this.costEstimatorForOpenPlans.cost(target)); 
	}

	/**
	 * Clone.
	 *
	 * @return SuccessDominance
	 */
	@Override
	public SuccessDominance clone() {
		return new SuccessDominance(this.costEstimatorForOpenPlans);
	}
}
