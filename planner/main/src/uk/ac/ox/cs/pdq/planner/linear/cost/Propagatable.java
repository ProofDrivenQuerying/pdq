package uk.ac.ox.cs.pdq.planner.linear.cost;

import uk.ac.ox.cs.pdq.planner.linear.node.SearchNode;

/**
 * Property of a cost model to be propagated across node of a search space.
 *
 * @author Julien Leblay
 */
public interface Propagatable {
	/**
	 * @return CostPropagator<T>
	 */
	<T extends SearchNode> CostPropagator<T> getPropagator();
}
