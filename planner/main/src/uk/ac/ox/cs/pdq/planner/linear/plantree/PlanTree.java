// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.linear.plantree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultEdge;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode;

/**
 * A tree of search nodes.
 *
 * @author Efthymia Tsamoura
 * @param <N> the number type
 */
public class PlanTree<N extends SearchNode> extends IndexedDirectedGraph<N> implements TreeGraph<N>{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8536976771945466744L;

	/**  The root of the tree. */
	private N root = null;

	/**  Maps each tree node to its ID. */
	private Map<Integer, N> map = new LinkedHashMap<>();

	/**
	 * Constructor for PlanTree.
	 * @param cl Class<DefaultEdge>
	 */
	public PlanTree(Class<DefaultEdge> cl) {
		super(cl);
	}

	/**
	 * Constructor for PlanTree.
	 * @param factory EdgeFactory<N,DefaultEdge>
	 */
	public PlanTree(EdgeFactory<N, DefaultEdge> factory) {
		super(factory);
	}

	/**
	 * Adds the vertex.
	 *
	 * @param n N
	 * @return boolean
	 */
	@Override
	public boolean addVertex(N n) {
		boolean added = super.addVertex(n);
		if (added) {
			this.map.put(n.getId(), n);
			if(this.root == null) {
				this.root = n;
			}
		}
		return added;
	}

	/**
	 * Gets the vertex.
	 *
	 * @param id Integer
	 * @return N
	 */
	@Override
	public N getVertex(Integer id) {
		return this.map.get(id);
	}

	/**
	 * Gets the root.
	 *
	 * @return N
	 * @see uk.ac.ox.cs.pdq.planner.linear.node.TreeGraph#getRoot()
	 */
	@Override
	public N getRoot() {
		return this.root;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.util.TreeGraph#getParent(java.lang.Object)
	 */
	@Override
	public N getParent(N child) {
		Preconditions.checkArgument(this.vertexSet().contains(child));
		Set<DefaultEdge> edges = this.incomingEdgesOf(child);
		if(edges.isEmpty()) {
			return null;
		}
		else {
			return this.getEdgeSource(edges.iterator().next());
		}
	}
	public List<N> getChildren(N parent) {
		Preconditions.checkArgument(this.vertexSet().contains(parent));
		Set<DefaultEdge> edges = this.outgoingEdgesOf(parent);
		if(edges.isEmpty()) {
			return new ArrayList<>();
		}
		else {
			List<N> children = new ArrayList<>();
			for (DefaultEdge edge:edges) {
				children.add(this.getEdgeTarget(edge));
			}
			return children;
		}
	}
}