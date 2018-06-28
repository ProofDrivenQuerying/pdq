package uk.ac.ox.cs.pdq.cost.estimators;


import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.AlgebraUtilities;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.DoubleCost;

/**
 * Cost estimator favoring query with more atoms.
 *
 * @author Julien Leblay
 * @param <P> the generic type
 */
public class LengthBasedCostEstimator implements OrderDependentCostEstimator {

	/**
	 * Clone.
	 *
	 * @return LengthBasedCostEstimator<P>
	 * @see uk.ac.ox.cs.pdq.plan.cost.CostEstimator#clone()
	 */
	@Override
	public LengthBasedCostEstimator clone() {
		return (LengthBasedCostEstimator) new LengthBasedCostEstimator();
	}

	/**
	 * Cost.
	 *
	 * @param term P
	 * @return Cost
	 * @see uk.ac.ox.cs.pdq.plan.cost.CostEstimator#cost(P)
	 */
	@Override
	public DoubleCost cost(RelationalTerm term) {
		List<AccessTerm> accesses = new ArrayList<>();
		for (AccessTerm access:AlgebraUtilities.getAccesses(term)) {
			if (!accesses.contains(access)) 
				accesses.add(access);
		}
		DoubleCost result = new DoubleCost(1.0 / accesses.size());
		return result;
	}
}
