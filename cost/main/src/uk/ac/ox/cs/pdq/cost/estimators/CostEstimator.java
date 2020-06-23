// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.cost.estimators;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;


/**
 * Returns the cost of a plan.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public interface CostEstimator extends Cloneable {
	
	/**
	 * Estimates and sets the cost of the input plan.
	 *
	 * @param plan the plan
	 * @return the cost of the input plan
	 */
	Cost cost(RelationalTerm plan);
	
	/**
	 * Clone.
	 *
	 * @return the cost estimator
	 */
	CostEstimator clone();
}
