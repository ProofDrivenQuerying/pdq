// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.dominance;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.estimators.OrderIndependentCostEstimator;

/**
 * Closed cost dominance. A closed plan p cost dominates another closed plan p',
 * if p is closed and has cost < the cost of p'.
 *
 * @author Efthymia Tsamoura
 * @author Gabor
 */

public class CostDominance {

	/** The estimator. */
	private final OrderIndependentCostEstimator costEstimatorForOpenPlans;

	/**
	 * Constructor for SuccessDominance.
	 * 
	 * @param simpleFunction
	 *            Boolean
	 */
	public CostDominance(OrderIndependentCostEstimator estimator) {
		Preconditions.checkNotNull(estimator);
		this.costEstimatorForOpenPlans = estimator;
	}

	/**
	 * Checks if is dominated.
	 *
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @return true if the source plan is success dominated by the target
	 */
	public boolean isDominated(RelationalTerm source, Cost sourceCost, RelationalTerm target, Cost targetCost) {
		if (source.isClosed() && target.isClosed())
			return sourceCost.greaterThan(targetCost);

		return this.costEstimatorForOpenPlans.cost(source).greaterThan(this.costEstimatorForOpenPlans.cost(target));
	}

	/**
	 * Clone.
	 *
	 * @return SuccessDominance
	 */
	@Override
	public CostDominance clone() {
		return new CostDominance(this.costEstimatorForOpenPlans);
	}
}
