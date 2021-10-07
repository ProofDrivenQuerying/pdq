// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.cost.estimators;


import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.DoubleCost;

import java.util.ArrayList;
import java.util.List;

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
