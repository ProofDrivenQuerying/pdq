package uk.ac.ox.cs.pdq.cost.estimators;

import uk.ac.ox.cs.pdq.util.Costable;

/**
 * Top level interface for all blackbox plan cost estimators.
 *	The cost of a plan depends on the operators' order
 *
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 */
public interface BlackBoxCostEstimator<P extends Costable> extends CostEstimator<P> {

	/**
	 * @return BlackBoxCostEstimator<P>
	 */
	BlackBoxCostEstimator<P> clone();
	
}
