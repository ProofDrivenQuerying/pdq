// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.cost.estimators;

/**
 * Top level interface for all Order Dependent cost estimators.
 * 	The cost of a plan depends on the operators' order
 *
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 */
public interface OrderDependentCostEstimator extends CostEstimator {

	/**
	 * 
	 *
	 * @return CostEstimator<P>
	 */
	OrderDependentCostEstimator clone();
	
}
