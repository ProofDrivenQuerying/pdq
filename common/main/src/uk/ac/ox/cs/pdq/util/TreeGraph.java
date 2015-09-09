package uk.ac.ox.cs.pdq.util;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

/**
 * A tree
 *
 * @author Efthymia Tsamoura
 */
public interface TreeGraph<N> extends Graph<N, DefaultEdge> {

	/**
	 *
	 * @return the tree's root
	 */
	public N getRoot();
	
	public N getParent(N child);
}
