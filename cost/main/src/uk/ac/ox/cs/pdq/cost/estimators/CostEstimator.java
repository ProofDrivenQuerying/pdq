package uk.ac.ox.cs.pdq.cost.estimators;

import uk.ac.ox.cs.pdq.cost.Costable;
import uk.ac.ox.cs.pdq.datasources.Cost;


// TODO: Auto-generated Javadoc
/**
 * Returns the cost of a plan.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 * @param <C> the generic type
 */
public interface CostEstimator<C extends Costable> {
	
	/**
	 * Estimates and sets the cost of the input plan.
	 *
	 * @param plan the plan
	 * @return the cost of the input plan
	 */
	Cost cost(C plan);
	
	/**
	 * Estimates the cost of the input plan.
	 *
	 * @param plan the plan
	 * @return the cost of the input plan
	 */
	Cost estimateCost(C plan);
	
	/**
	 * Clone.
	 *
	 * @return the cost estimator
	 */
	CostEstimator<C> clone();
}
