package uk.ac.ox.cs.pdq.cost.estimators;

import java.util.Collection;

import uk.ac.ox.cs.pdq.plan.AccessOperator;
import uk.ac.ox.cs.pdq.plan.Cost;
import uk.ac.ox.cs.pdq.util.Costable;

/**
 * Top level interface for all simple plan cost estimators.
 * The cost of a plan does not depend on the operators' order
 *
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 */
public interface SimpleCostEstimator<P extends Costable> extends CostEstimator<P> {
	/**
	 * @param accesses The accesses of a plan
	 * @return the cost of the accesses
	 * 
	 */
	Cost cost(Collection<AccessOperator> accesses);

	/**
	 * @return SimpleCostEstimator<P>
	 */
	SimpleCostEstimator<P> clone();
}
