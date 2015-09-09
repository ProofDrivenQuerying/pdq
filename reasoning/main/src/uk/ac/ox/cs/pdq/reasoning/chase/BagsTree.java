package uk.ac.ox.cs.pdq.reasoning.chase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultEdge;

import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.reasoning.chase.Bag.BagStatus;
import uk.ac.ox.cs.pdq.util.IndexedDirectedGraph;
import uk.ac.ox.cs.pdq.util.TreeGraph;

import com.google.common.base.Preconditions;

/**
 * A tree of bags
 *
 * @author Efthymia Tsamoura
 *
 * @param <Bag>
 * 		The type of the input bags
 */

public class BagsTree extends IndexedDirectedGraph<Bag> implements TreeGraph<Bag>{

	/** The root of the tree*/
	private Bag root = null;

	/** Maps each tree node to its ID*/
	private final Map<Integer, Bag> bags = new LinkedHashMap<>();
	
	private final Set<Predicate> facts = new HashSet<>();
	
	private static BagFactory bagFactory;

	/**
	 * Constructor for BagsTree.
	 * @param cl Class<DefaultEdge>
	 */
	public BagsTree(Class<DefaultEdge> cl) {
		super(cl);
	}

	/**
	 * Constructor for BagsTree.
	 * @param factory EdgeFactory<Bag,DefaultEdge>
	 */
	public BagsTree(EdgeFactory<Bag, DefaultEdge> factory) {
		super(factory);
	}

	/**
	 * @param n Bag
	 * @return boolean
	 */
	@Override
	public boolean addVertex(Bag n) {
		boolean added = super.addVertex(n);
		if (added) {
			this.bags.put(n.getId(), n);
			if(this.root == null) {
				this.root = n;
			}
		}
		for (Predicate fact:n.getFacts()) {
			this.facts.add(new BagBoundPredicate(fact, n.getId()));
		}
		return added;
	}
	
    /**
     * Removes all the edges in this graph that are also contained in the
     * specified edge collection. After this call returns, this graph will
     * contain no edges in common with the specified edges. This method will
     * invoke the {@link #removeEdge(Object)} method.
     *
     * @param edges edges to be removed from this graph.
     *
     * @return <tt>true</tt> if this graph changed as a result of the call
     *
     * @throws NullPointerException if the specified edge collection is <tt>
     * null</tt>.
     *
     * @see #removeEdge(Object)
     * @see #containsEdge(Object)
     */
	@Override
    public boolean removeAllEdges(Collection<? extends DefaultEdge> edges) {
    	throw new java.lang.UnsupportedOperationException("Cannot remove edges from a tree");	
    }

    /**
     * Removes all the edges going from the specified source vertex to the
     * specified target vertex, and returns a set of all removed edges. Returns
     * <code>null</code> if any of the specified vertices does not exist in the
     * graph. If both vertices exist but no edge is found, returns an empty set.
     * This method will either invoke the {@link #removeEdge(Object)} method, or
     * the {@link #removeEdge(Object, Object)} method.
     *
     * @param sourceVertex source vertex of the edge.
     * @param targetVertex target vertex of the edge.
     *
     * @return the removed edges, or <code>null</code> if no either vertex not
     * part of graph
     */
    @Override
    public Set<DefaultEdge> removeAllEdges(Bag sourceVertex, Bag targetVertex) {
    	throw new java.lang.UnsupportedOperationException("Cannot remove edges from a tree");	
    }

    /**
     * Removes all the vertices in this graph that are also contained in the
     * specified vertex collection. After this call returns, this graph will
     * contain no vertices in common with the specified vertices. This method
     * will invoke the {@link #removeVertex(Object)} method.
     *
     * @param vertices vertices to be removed from this graph.
     *
     * @return <tt>true</tt> if this graph changed as a result of the call
     *
     * @throws NullPointerException if the specified vertex collection is <tt>
     * null</tt>.
     *
     * @see #removeVertex(Object)
     * @see #containsVertex(Object)
     */
    @Override
    public boolean removeAllVertices(Collection<? extends Bag> vertices) {
    	throw new java.lang.UnsupportedOperationException("Cannot remove bags from a tree");	
    }

    /**
     * Removes an edge going from source vertex to target vertex, if such
     * vertices and such edge exist in this graph. Returns the edge if removed
     * or <code>null</code> otherwise.
     *
     * @param sourceVertex source vertex of the edge.
     * @param targetVertex target vertex of the edge.
     *
     * @return The removed edge, or <code>null</code> if no edge removed.
     */
    @Override
    public DefaultEdge removeEdge(Bag sourceVertex, Bag targetVertex) {
    	throw new java.lang.UnsupportedOperationException("Cannot remove edges from a tree");	
    }

    /**
     * Removes the specified edge from the graph. Removes the specified edge
     * from this graph if it is present. More formally, removes an edge <code>
     * e2</code> such that <code>e2.equals(e)</code>, if the graph contains such
     * edge. Returns <tt>true</tt> if the graph contained the specified edge.
     * (The graph will not contain the specified edge once the call returns).
     *
     * <p>If the specified edge is <code>null</code> returns <code>
     * false</code>.</p>
     *
     * @param e edge to be removed from this graph, if present.
     *
     * @return <code>true</code> if and only if the graph contained the
     * specified edge.
     */
    @Override
    public boolean removeEdge(DefaultEdge e) {
    	throw new java.lang.UnsupportedOperationException("Cannot remove edges from a tree");	
    }

    /**
     * Removes the specified vertex from this graph including all its touching
     * edges if present. More formally, if the graph contains a vertex <code>
     * u</code> such that <code>u.equals(v)</code>, the call removes all edges
     * that touch <code>u</code> and then removes <code>u</code> itself. If no
     * such <code>u</code> is found, the call leaves the graph unchanged.
     * Returns <tt>true</tt> if the graph contained the specified vertex. (The
     * graph will not contain the specified vertex once the call returns).
     *
     * <p>If the specified vertex is <code>null</code> returns <code>
     * false</code>.</p>
     *
     * @param v vertex to be removed from this graph, if present.
     *
     * @return <code>true</code> if the graph contained the specified vertex;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean removeVertex(Bag v) {
    	throw new java.lang.UnsupportedOperationException("Cannot remove bags from a tree");
    }


	/**
	 * @param fact
	 * @return a bag (randomly chosen) that contains the input fact
	 */
	public Bag getBag(Predicate fact) {
		for (Bag bag:this.vertexSet()) {
			if(bag.getFacts().contains(fact)) {
				return bag;
			}
		}
		return null;
	}

	/**
	 * @param id Integer
	 * @return Bag
	 */
	@Override
	public Bag getVertex(Integer id) {
		return this.bags.get(id);
	}

	/**
	 * @return Bag
	 * @see uk.ac.ox.cs.pdq.planner.linear.node.TreeGraph#getRoot()
	 */
	@Override
	public Bag getRoot() {
		return this.root;
	}

	/**
	 * @return the facts of the bags
	 */
	public Collection<Predicate> getFacts() {
		return this.facts;
	}
	
	/**
	 * 
	 * @param status
	 * @return
	 * 		the bags with the input status
	 */
	public Bag[] getBags(BagStatus status) {
		Collection<Bag> bags = new ArrayList<>();
		for (Bag bag:this.vertexSet()) {
			if (status == null || bag.getType() == status) {
				bags.add(bag);
			}
		}
		return bags.toArray(new Bag[bags.size()]);
	}

	/**
	 * @return BagsTree<Bag>
	 */
	@Override
	public BagsTree clone() {
		BagsTree tree = new BagsTree(this.getEdgeFactory());

		Set<Bag> bags = this.vertexSet();
		Map<Bag,Bag> map =  new LinkedHashMap<>();

		for (Bag bag: bags) {
			Bag clone = (Bag) bag.clone();
			tree.addVertex(clone);
			map.put(bag, clone);
		}

		for (DefaultEdge edge: this.edgeSet()) {
			Bag source = this.getEdgeSource(edge);
			Bag target = this.getEdgeTarget(edge);
			Bag mySource = map.get(source);
			Bag myTarget = map.get(target);
			tree.addEdge(mySource, myTarget, this.getEdgeFactory().createEdge(mySource, myTarget));
		}
		return tree;
	}

	/**
	 * @return a tree with the same facts within a bag, but with different bag IDs
	 */
	public BagsTree replicate() {
		BagsTree tree = new BagsTree(this.getEdgeFactory());

		Set<Bag> bags = this.vertexSet();
		Map<Bag,Bag> map =  new LinkedHashMap<>();

		for (Bag bag: bags) {
			Bag clone = (Bag) bag.replicate();
			tree.addVertex(clone);
			map.put(bag, clone);
		}

		for (DefaultEdge edge: this.edgeSet()) {
			Bag source = this.getEdgeSource(edge);
			Bag target = this.getEdgeTarget(edge);
			Bag mySource = map.get(source);
			Bag myTarget = map.get(target);
			tree.addEdge(mySource, myTarget, this.getEdgeFactory().createEdge(mySource, myTarget));
		}
		return tree;
	}

	@Override
	public Bag getParent(Bag child) {
		Preconditions.checkArgument(this.vertexSet().contains(child));
		Set<DefaultEdge> edges = this.incomingEdgesOf(child);
		if(edges.isEmpty()) {
			return null;
		}
		else {
			return this.getEdgeSource(edges.iterator().next());
		}
	}

	public BagFactory getBagFactory() {
		return BagsTree.bagFactory;
	}
	
	public static void setBagFactory(BagFactory factory) {
		BagsTree.bagFactory = factory;
	}
	
	/**
	 *
	 * @param facts
	 * @return
	 * 		a tree with root a bag containing the input facts
	 */
	public static BagsTree initialiseTree(Collection<Predicate> facts) {
		BagsTree bagsTree = new BagsTree(DefaultEdge.class);
		Bag root = BagsTree.bagFactory.createBag(facts);
		bagsTree.addVertex(root);
		return bagsTree;
	}
}
