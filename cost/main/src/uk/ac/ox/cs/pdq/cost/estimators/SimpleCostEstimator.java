package uk.ac.ox.cs.pdq.cost.estimators;

import java.util.Collection;

import uk.ac.ox.cs.pdq.cost.Costable;
import uk.ac.ox.cs.pdq.datasources.Cost;
import uk.ac.ox.cs.pdq.plan.AccessOperator;

// TODO: Auto-generated Javadoc
/**
 * Top level interface for all simple plan cost estimators.
 * The cost of a plan does not depend on the operators' order
 *
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 * @param <P> the generic type
 */
public interface SimpleCostEstimator<P extends Costable> extends CostEstimator<P> {
	
	/**
	 * Cost.
	 *
	 * @param accesses The accesses of a plan
	 * @return the cost of the accesses
	 */
	Cost cost(Collection<AccessOperator> accesses);

	/**
	 * Clone.
	 *
	 * @return SimpleCostEstimator<P>
	 */
	SimpleCostEstimator<P> clone();
}
