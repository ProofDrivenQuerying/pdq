package uk.ac.ox.cs.pdq.planner.linear.cost;

import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;

// TODO: Auto-generated Javadoc
/**
 * Property of a cost model to be propagated across node of a search space.
 *
 * @author Julien Leblay
 */
public interface Propagatable {
	
	/**
	 * Gets the propagator.
	 *
	 * @param <T> the generic type
	 * @return CostPropagator<T>
	 */
	<T extends SearchNode> CostPropagator<T> getPropagator();
}
