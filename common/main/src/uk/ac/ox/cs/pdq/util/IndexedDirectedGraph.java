package uk.ac.ox.cs.pdq.util;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * Directed graph that allows reaching any node by its ID.
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public abstract class IndexedDirectedGraph<N> extends DefaultDirectedGraph<N, DefaultEdge> {
	/** */
	private static final long serialVersionUID = -5341706482975843364L;

	/**
	 * Constructor for IndexedDirectedGraph.
	 * @param cl Class<DefaultEdge>
	 */
	public IndexedDirectedGraph(Class<DefaultEdge> cl) {
		super(cl);
	}

	/**
	 * Constructor for IndexedDirectedGraph.
	 * @param factory EdgeFactory<N,DefaultEdge>
	 */
	public IndexedDirectedGraph(EdgeFactory<N, DefaultEdge> factory) {
		super(factory);
	}

	/**
	 * @param id
	 * @return the vertex having the input id
	 */
	public abstract N getVertex(Integer id);

	/**
	 * @param nodeSequence
	 * @return the vertices having the input identifiers.
	 */
	public List<N> getPath(List<Integer> nodeSequence) {
		if (nodeSequence != null && !nodeSequence.isEmpty()) {
			List<N> nodes = new ArrayList<>();
			for (Integer n: nodeSequence) {
				N node = this.getVertex(n);
				if (node == null) {
					throw new IllegalStateException("Vertex " + n + " not found in node set.");
				}
				nodes.add(node);
			}
			return nodes;
		}
		return null;
	}
}
