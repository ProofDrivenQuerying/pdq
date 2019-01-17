package uk.ac.ox.cs.pdq.cost.estimators;

import java.util.Collection;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.DoubleCost;


/**
 * A simple cost estimator.
 * According to this implementation, the cost of a plan equals the number of the accesses.
 *
 * @author Efthymia Tsamoura
 */
public class CountNumberOfAccessedRelationsCostEstimator implements OrderIndependentCostEstimator {

	/**
	 * Clone.
	 *
	 * @return SimpleCountCostEstimator<P,S>
	 * @see uk.ac.ox.cs.pdq.cost.estimators.OrderIndependentCostEstimator#clone()
	 */
	@Override
	public CountNumberOfAccessedRelationsCostEstimator clone() {
		return new CountNumberOfAccessedRelationsCostEstimator();
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
		DoubleCost result = this.cost(plan.getAccesses());
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
		DoubleCost result = new DoubleCost(accesses.size());
		return result;
	}
}
