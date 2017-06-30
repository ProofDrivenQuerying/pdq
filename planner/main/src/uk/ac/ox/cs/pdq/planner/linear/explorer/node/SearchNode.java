package uk.ac.ox.cs.pdq.planner.linear.explorer.node;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.dominance.FactDominance;
import uk.ac.ox.cs.pdq.planner.dominance.FastFactDominance;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.Metadata;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
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
		
		/** The fake terminal. */
		FAKE_TERMINAL
	}

	/** Status of the current node. ONGOING by default */
	protected NodeStatus status = NodeStatus.ONGOING;

	/**  The node's id. */
	private final int id;

	/** The global id. */
	protected static int globalId = 0;

	/**  The node's configuration. */
	private final LinearChaseConfiguration configuration;

	/** Pointer node. Pointers are created during global equivalence checks */
	private SearchNode pointer = null;

	/**  The node's depth. */
	private int depth = 0;

	/** The metadata. */
	private Metadata metadata = null;

	/**  True if the node is fully generated. */
	private Boolean isFullyGenerated = false;
	
	/**  The path from root. */
	private final List<Integer> pathFromRoot;

	/**  The best path from root. */
	private List<Integer> bestPathFromRoot = null;

	/**  The path plan from root. */
	private LeftDeepPlan bestPlanFromRoot = null;

	/**  The plan that cost dominates the node. */
	private LeftDeepPlan dominancePlan = null;

	/**
	 * Instantiates a new search node.
	 *
	 * @param configuration The configuration of the node
	 * @throws PlannerException the planner exception
	 */
	public SearchNode(LinearChaseConfiguration configuration) throws PlannerException {
		this.id = globalId++;
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
		this.id = globalId++;
		this.depth = parent.getDepth() + 1;
		this.configuration = configuration;
		List<Integer> pathFromRoot = null;
		if(parent.getPathFromRoot() == null) {
			pathFromRoot = Lists.newArrayList();
		}
		else {
			pathFromRoot = Lists.newArrayList(parent.getPathFromRoot());
		}
		pathFromRoot.add(this.id);
		this.pathFromRoot = pathFromRoot;

		List<Integer> bestPathFromRoot = null;
		if(parent.getBestPathFromRoot() == null) {
			bestPathFromRoot = Lists.newArrayList();
		}
		else {
			bestPathFromRoot = Lists.newArrayList(parent.getBestPathFromRoot());
		}
		bestPathFromRoot.add(this.getId());
		this.setBestPathFromRoot(bestPathFromRoot);
		this.setBestPlanFromRoot(this.configuration.getPlan());
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
	public void close(Chaser chaser, ConjunctiveQuery query, Collection<? extends Dependency> dependencies) throws PlannerException, LimitReachedException {
		this.configuration.reasonUntilTermination(chaser, query, dependencies);
		this.isFullyGenerated = true;
	}

	/**
	 * Checks if is dominated by.
	 *
	 * @param target the target
	 * @return true if the current node is globally dominated by the input one
	 */
	public boolean isDominatedBy(SearchNode target) {
		return new Dominance().isDominated(this, target);
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
	public void setPointer(SearchNode pointer) {
		this.pointer = pointer;
	}

	/**
	 * Gets the pointer.
	 *
	 * @return the pointer
	 */
	public SearchNode getPointer() {
		return this.pointer;
	}

	/**
	 * Sets the depth.
	 *
	 * @param depth the new depth
	 */
	public void setDepth(int depth) {
		this.depth = depth;
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
	public Boolean isFullyGenerated() {
		return this.isFullyGenerated;
	}


	/**
	 * Sets the checks if is fully generated.
	 *
	 * @param isFullyGenerated the new checks if is fully generated
	 */
	public void setIsFullyGenerated(Boolean isFullyGenerated) {
		this.isFullyGenerated = isFullyGenerated;
	}

	/**
	 * Gets the metadata.
	 *
	 * @return the metadata
	 */
	public Metadata getMetadata() {
		return this.metadata;
	}


	/**
	 * Sets the metadata.
	 *
	 * @param metadata the new metadata
	 */
	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
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
	public LeftDeepPlan getBestPlanFromRoot() {
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
	public void setBestPlanFromRoot(LeftDeepPlan planFromRoot) {
		this.bestPlanFromRoot = planFromRoot;
	}

	/**
	 * Gets the dominance plan.
	 *
	 * @return LeftDeepPlan
	 */
	public LeftDeepPlan getDominancePlan() {
		return this.dominancePlan;
	}

	/**
	 * Sets the dominance plan.
	 *
	 * @param dominancePlan LeftDeepPlan
	 */
	public void setDominancePlan(LeftDeepPlan dominancePlan) {
		this.dominancePlan = dominancePlan;
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

	/**
	 * The Class Dominance.
	 *
	 * @author Efthymia Tsamoura
	 */
	private class Dominance {
		
		/** The fact dominance. */
		private final FactDominance factDominance = new FastFactDominance(false);

		/**
		 * Checks if is dominated.
		 *
		 * @param source SearchNode
		 * @param target SearchNode
		 * @return true if the source is dominated by the target
		 */
		public boolean isDominated(SearchNode source, SearchNode target) {
			if(source.getBestPlanFromRoot() != null &&
					target.getBestPlanFromRoot() != null &&
					source.getBestPlanFromRoot().getCost().greaterOrEquals(target.getBestPlanFromRoot().getCost())) {
				return this.factDominance.isDominated(source.getConfiguration(), target.getConfiguration());
			}
			return false;
		}
	}

}
