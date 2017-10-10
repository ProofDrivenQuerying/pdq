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
	 * Cost.
	 *
	 * @param accesses The accesses of a plan
	 * @return the cost of the accesses
	 */
	Cost cost(Collection<AccessTerm> accesses);

	/**
	 * Clone.
	 *
	 * @return SimpleCostEstimator<P>
	 */
	OrderIndependentCostEstimator clone();
}
