package uk.ac.ox.cs.pdq.cost.estimators;

// TODO: Auto-generated Javadoc
/**
 * Top level interface for all blackbox plan cost estimators.
 * 	The cost of a plan depends on the operators' order
 *
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 */
public interface OrderDependentCostEstimator extends CostEstimator {

	/**
	 * Clone.
	 *
	 * @return BlackBoxCostEstimator<P>
	 */
	OrderDependentCostEstimator clone();
	
}
