// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.cost.estimators;


import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.DoubleCost;

/**
 * Cost estimator favoring query with fewer atoms.
 *
 * @author Julien Leblay
 * 
 */
public class LengthBasedCostEstimator implements OrderDependentCostEstimator {

	/**
	 * 
	 *
	 * @return LengthBasedCostEstimator
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
		for (AccessTerm access:term.getAccesses()) {
			if (!accesses.contains(access)) 
				accesses.add(access);
		}
		DoubleCost result = new DoubleCost(1.0 / accesses.size());
		return result;
	}
}
