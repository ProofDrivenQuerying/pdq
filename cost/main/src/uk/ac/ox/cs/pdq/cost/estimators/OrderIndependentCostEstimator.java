// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.cost.estimators;

import java.util.Collection;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.cost.Cost;

/**
 * Top level interface for all simple plan cost estimators.
 * The cost of a plan does not depend on the operators' order
 *
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 * @param <P> the generic type
 */
public interface OrderIndependentCostEstimator extends CostEstimator {
	
	/**
	 * 
	 *
	 * @param accesses The accesses of a plan
	 * @return the cost of the accesses
	 */
	Cost cost(Collection<AccessTerm> accesses);

	/**
	 * 
	 *
	 * @return SimpleCostEstimator<P>
	 */
	OrderIndependentCostEstimator clone();
}
