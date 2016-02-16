package uk.ac.ox.cs.pdq.util;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

// TODO: Auto-generated Javadoc
/**
 * A tree.
 *
 * @author Efthymia Tsamoura
 * @param <N> the number type
 */
public interface TreeGraph<N> extends Graph<N, DefaultEdge> {

	/**
	 * Gets the root.
	 *
	 * @return the tree's root
	 */
	public N getRoot();
	
	/**
	 * Gets the parent.
	 *
	 * @param child the child
	 * @return the parent
	 */
	public N getParent(N child);
}
