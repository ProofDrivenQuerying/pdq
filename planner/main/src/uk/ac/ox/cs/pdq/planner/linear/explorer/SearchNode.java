package uk.ac.ox.cs.pdq.planner.linear.explorer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.dominance.FactDominance;
import uk.ac.ox.cs.pdq.planner.dominance.FastFactDominance;
import uk.ac.ox.cs.pdq.planner.equivalence.FactEquivalence;
import uk.ac.ox.cs.pdq.planner.equivalence.FastFactEquivalence;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.plantree.PlanTree;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;
import uk.ac.ox.cs.pdq.util.LimitReachedException;


/**
 * The linear plans that are visited during exploration are organised into a tree. 
 * The nodes of this tree correspond to (partial) linear configurations. 
 * This class provides an abstract node class.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public abstract class SearchNode implements Cloneable{

	/**
	 *  Possible status of node during a search.
	 */
	public static enum NodeStatus {
		
		/**  A node where a query match is found. */
		SUCCESSFUL,
		
		/**  A node that is not SUCCESSFUL and has at least one unexposed candidate fact. */
		ONGOING,
		
		/**  A node under which no path will not be explored. */
		TERMINAL,
	}

	/** Status of the current node. ONGOING by default */
	protected NodeStatus status = NodeStatus.ONGOING;

	/**  The node's id. */
	private final int id;

	/**  The node's configuration. */
	private final LinearChaseConfiguration configuration;

	/** Pointer node. Pointers are created during global equivalence checks */
	private SearchNode equivalentNode = null;

	/**  The node's depth. */
	private final int depth;

	/**  True if the node is fully generated. */
	private Boolean isFullyChased = false;
	
	/**  The path from root. */
	private final List<Integer> pathFromRoot;

	/**  The best path from root. */
	private List<Integer> bestPathFromRoot = null;

	/**  The path plan from root. */
	private RelationalTerm bestPlanFromRoot = null;
	
	private Cost costOfBestPlanFromRoot = null;

	/**  The plan that cost dominates the node. */
	private RelationalTerm dominatingPlan = null;
	
	private Cost costOfDominatingPlan = null;

	/**
	 * Instantiates a new search node.
	 *
	 * @param configuration The configuration of the node
	 * @throws PlannerException the planner exception
	 */
	public SearchNode(LinearChaseConfiguration configuration) throws PlannerException {
		this.depth = 0;
		this.id = GlobalCounterProvider.getNext("SearchNodeID");
		this.configuration = configuration;
		this.pathFromRoot = null;
	}

	/**
	 * Instantiates a new search node.
	 *
	 * @param parent The parent node
	 * @param configuration The configuration of the node
	 * @throws PlannerException the planner exception
	 */
	public SearchNode(SearchNode parent, LinearChaseConfiguration configuration) throws PlannerException {
		this.id = GlobalCounterProvider.getNext("SearchNodeID");
		this.depth = parent.getDepth() + 1;
		this.configuration = configuration;
		List<Integer> pathFromRoot = null;
		if(parent.getPathFromRoot() == null) 
			pathFromRoot = new ArrayList<>();
		else 
			pathFromRoot = new ArrayList<>(parent.getPathFromRoot());
		pathFromRoot.add(this.id);
		this.pathFromRoot = pathFromRoot;

		List<Integer> bestPathFromRoot = null;
		if(parent.getBestPathFromRoot() == null) 
			bestPathFromRoot = new ArrayList<>();
		else 
			bestPathFromRoot = new ArrayList<>(parent.getBestPathFromRoot());
		bestPathFromRoot.add(this.getId());
		this.bestPathFromRoot = bestPathFromRoot;
		this.bestPlanFromRoot = this.configuration.getPlan();
	}

	/**
	 * Searches for query matches in the current configuration.
	 *
	 * @param query the query
	 * @return the list of matches
	 * @throws PlannerException the planner exception
	 * @throws LimitReachedException the limit reached exception
	 */
	public List<Match> matchesQuery(ConjunctiveQuery query) throws PlannerException, LimitReachedException {
		return this.configuration.matchesQuery(query);
	}

	/**
	 * Closes the configuration of this node by adding consequences, stopping if the  query is true
	 *
	 * @param chaser the chaser
	 * @param query the query
	 * @param dependencies the dependencies
	 * @throws PlannerException the planner exception
	 * @throws LimitReachedException the limit reached exception
	 */
	public void close(Chaser chaser, Dependency[] dependencies) throws PlannerException, LimitReachedException {
		this.configuration.reasonUntilTermination(chaser, dependencies);
		this.isFullyChased = true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}

	/**
	 * Gets the configuration.
	 *
	 * @return the configuration
	 */
	public LinearChaseConfiguration getConfiguration() {
		return this.configuration;
	}

	/**
	 * Sets the status.
	 *
	 * @param s the new status
	 */
	public void setStatus(NodeStatus s) {
		this.status = s;
	}

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public NodeStatus getStatus() {
		return this.status;
	}

	/**
	 * Sets the pointer.
	 *
	 * @param pointer The pointer node
	 */
	public void setEquivalentNode(SearchNode pointer) {
		this.equivalentNode = pointer;
	}

	/**
	 * Gets the pointer.
	 *
	 * @return the pointer
	 */
	public SearchNode getEquivalentNode() {
		return this.equivalentNode;
	}

	/**
	 * Gets the depth.
	 *
	 * @return the depth
	 */
	public int getDepth() {
		return this.depth;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * Checks if is fully generated.
	 *
	 * @return true if the configuration is fully closed
	 */
	public Boolean isFullyChased() {
		return this.isFullyChased;
	}


	/**
	 * Sets the checks if is fully generated.
	 *
	 * @param isFullyGenerated the new checks if is fully generated
	 */
	public void setIsFullyGenerated(Boolean isFullyGenerated) {
		this.isFullyChased = isFullyGenerated;
	}
	
	/**
	 * Gets the best path from root.
	 *
	 * @return the best path from root
	 */
	public List<Integer> getBestPathFromRoot() {
		return this.bestPathFromRoot;
	}

	/**
	 * Gets the path from root.
	 *
	 * @return List<Integer>
	 */
	public List<Integer> getPathFromRoot() {
		return this.pathFromRoot;
	}

	/**
	 * Gets the best plan from root.
	 *
	 * @return LeftDeepPlan
	 */
	public RelationalTerm getBestPlanFromRoot() {
		return this.bestPlanFromRoot;
	}

	/**
	 * Sets the best path from root.
	 *
	 * @param pathFromRoot List<Integer>
	 */
	public void setBestPathFromRoot(List<Integer> pathFromRoot) {
		this.bestPathFromRoot = pathFromRoot;
	}

	/**
	 * Sets the best plan from root.
	 *
	 * @param planFromRoot LeftDeepPlan
	 */
	public void setBestPlanFromRoot(RelationalTerm planFromRoot) {
		this.bestPlanFromRoot = planFromRoot;
	}

	/**
	 * Gets the dominance plan.
	 *
	 * @return LeftDeepPlan
	 */
	public RelationalTerm getDominatingPlan() {
		return this.dominatingPlan;
	}

	/**
	 * Sets the dominance plan.
	 *
	 * @param dominatingPlan LeftDeepPlan
	 */
	public void setDominatingPlan(RelationalTerm dominatingPlan) {
		this.dominatingPlan = dominatingPlan;
	}

	/**
	 * To string.
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		return String.valueOf(this.id);
	}

	/**
	 * Equals.
	 *
	 * @param o Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return this.getClass().isInstance(o)
				&& this.id == ((SearchNode) o).id;
	}

	/**
	 * Sets the path to success.
	 *
	 * @param pathToSuccess List<Integer>
	 */
	public abstract void setPathToSuccess(List<Integer> pathToSuccess);

	public Cost getCostOfDominatingPlan() {
		return this.costOfDominatingPlan;
	}

	public void setCostOfDominatingPlan(Cost costOfDominatingPlan) {
		this.costOfDominatingPlan = costOfDominatingPlan;
	}

	public Cost getCostOfBestPlanFromRoot() {
		return costOfBestPlanFromRoot;
	}

	public void setCostOfBestPlanFromRoot(Cost costOfBestPlanFromRoot) {
		this.costOfBestPlanFromRoot = costOfBestPlanFromRoot;
	}

	/**
	 * Julien: Quick fix to avoid cycles in propagation.
	 *
	 * @param <N> the number type
	 * @param planTree the plan tree
	 * @param node the node
	 * @return a collection of the ancestors of snode
	 */
	public static <N extends SearchNode> Collection<N> ancestorsOf(PlanTree<N> planTree, N node) {
		Collection<N> result = new LinkedHashSet<>();
		for (DefaultEdge n:planTree.incomingEdgesOf(node)) {
			N sourceNode = planTree.getEdgeSource(n);
			if (!result.contains(sourceNode)) 
				result.addAll(ancestorsOf(planTree, sourceNode));
			result.add(sourceNode);
		}
		return result;
	}

	/**
	 * Julien: Quick fix to avoid cycles in propagation.
	 *
	 * @param <N> the number type
	 * @param planTree PlanTree<N>
	 * @param node N
	 * @return a collection of all node in the search space after removing the
	 * ancestors of snode
	 */
	public static <N extends SearchNode> Collection<N> allButAncestorsOf(PlanTree<N> planTree, N node) {
		Collection<N> result = new LinkedHashSet<>(planTree.vertexSet());
		result.removeAll(SearchNode.ancestorsOf(planTree, node));
		return result;
	}

	/**
	 *TOCOMMENT: WHAT ARE THESE!!! 
	 *
	 * @param <N> the number type
	 * @param planTree the plan tree
	 * @return the partially generated leaves
	 */
	public static <N extends SearchNode> Collection<N> getLeafNodesThatAreNotFullyChased(DirectedGraph<N, DefaultEdge> planTree) {
		Collection<N> partiallyGenerated = new LinkedHashSet<>();
		for (N node:planTree.vertexSet()) {
			if (planTree.outDegreeOf(node) == 0 && !node.isFullyChased()) 
				partiallyGenerated.add(node);
		}
		return partiallyGenerated;
	}

	/**
	 * Gets the fully generated nodes.
	 *
	 * @param <N> the number type
	 * @param planTree the plan tree
	 * @return the fully generate nodes
	 */
	public static <N extends SearchNode> Collection<N> getNodesThatAreFullyChased(DirectedGraph<N, DefaultEdge> planTree) {
		Collection<N> fullyGenerated = new LinkedHashSet<>();
		for (N node: planTree.vertexSet()) {
			if (node.isFullyChased()) 
				fullyGenerated.add(node);
		}
		return fullyGenerated;
	}

	/**
	 *
	 * @param <N> the number type
	 * @param parentsNodes the parents nodes
	 * @param childNode the child node
	 * @return a parent node that dominates the child
	 */
	public static <N extends SearchNode> N isCostAndFactDominated(Collection<N> parentsNodes, N childNode) {
		FactDominance factDominance = new FastFactDominance(false);
		for (N parentNode: parentsNodes) {
			if (!parentNode.equals(childNode)
					&& !parentNode.getStatus().equals(NodeStatus.TERMINAL) &&
					childNode.getCostOfBestPlanFromRoot() != null &&
					parentNode.getCostOfBestPlanFromRoot() != null &&
					childNode.getCostOfBestPlanFromRoot().greaterOrEquals(parentNode.getCostOfBestPlanFromRoot()) &&
					factDominance.isDominated(childNode.getConfiguration(), parentNode.getConfiguration())) {
				return parentNode;
			}
		}
		return null;
	}

	/**
	 *
	 * @param <N> the number type
	 * @param parentsNodes the parents nodes
	 * @param childNode the child node
	 * @return a parent node that is globally equivalent to the input node
	 */
	public static <N extends SearchNode> N isEquivalent(Collection<N> parentsNodes, N childNode) {
		FactEquivalence factEquivalence = new FastFactEquivalence();
		N equivalent = null;
		Preconditions.checkArgument(childNode.isFullyChased());
		for (N parentNode: parentsNodes) {
			if (!parentNode.equals(childNode)
					&& parentNode.isFullyChased()
					&& parentNode.getEquivalentNode() == null
					&& !parentNode.getStatus().equals(NodeStatus.TERMINAL)
					//					&& !parentNode.getStatus().equals(NodeStatus.FAKE_TERMINAL)
					&& factEquivalence.isEquivalent(childNode.getConfiguration(), parentNode.getConfiguration())) {
				if (equivalent == null) 
					equivalent = parentNode;
				else {
					if (parentNode.getId() < equivalent.getId()) 
						equivalent = parentNode;
				}
			}
		}
		return equivalent;
	}

}
