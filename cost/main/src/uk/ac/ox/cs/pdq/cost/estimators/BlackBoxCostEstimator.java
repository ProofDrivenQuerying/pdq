package uk.ac.ox.cs.pdq.cost.estimators;

import uk.ac.ox.cs.pdq.cost.Costable;

// TODO: Auto-generated Javadoc
/**
 * Top level interface for all blackbox plan cost estimators.
 * 	The cost of a plan depends on the operators' order
 *
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 * @param <P> the generic type
 */
public interface BlackBoxCostEstimator<P extends Costable> extends CostEstimator<P> {

	/**
	 * Clone.
	 *
	 * @return BlackBoxCostEstimator<P>
	 */
	BlackBoxCostEstimator<P> clone();
	
}
