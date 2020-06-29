// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.linear.plantree;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

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
