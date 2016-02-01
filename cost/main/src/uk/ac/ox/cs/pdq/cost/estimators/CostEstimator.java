package uk.ac.ox.cs.pdq.cost.estimators;

import uk.ac.ox.cs.pdq.plan.Cost;
import uk.ac.ox.cs.pdq.util.Costable;


/**
 * Returns the cost of a plan.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */
public interface CostEstimator<C extends Costable> {
	/**
	 * Estimates and sets the cost of the input plan
	 * @param plan
	 * @return the cost of the input plan
	 */
	Cost cost(C plan);
	
	/**
	 * Estimates the cost of the input plan
	 * @param plan
	 * @return the cost of the input plan
	 */
	Cost estimateCost(C plan);
	
	CostEstimator<C> clone();
}
