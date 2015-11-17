package uk.ac.ox.cs.pdq.planner.linear.node;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.metadata.Metadata;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.FactDominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.FastFactDominance;
import uk.ac.ox.cs.pdq.reasoning.Match;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;

import com.google.common.collect.Lists;

/**
 * A node in the plan tree.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public abstract class SearchNode implements Cloneable{

	/** Possible status of node during a search */
	public static enum NodeStatus {
		/** A node where a query match is found */
		SUCCESSFUL,
		/** A node that is not SUCCESSFUL and has at least one unexposed candidate fact */
		ONGOING,
		/** A node under which no path will not be explored */
		TERMINAL,
		FAKE_TERMINAL
	}

	/** Status of the current node. ONGOING by default */
	protected NodeStatus status = NodeStatus.ONGOING;

	/** The node's id*/
	private final int id;

	protected static int globalId = 0;

	/** The node's configuration */
	private final LinearChaseConfiguration configuration;

	/** Pointer node. Pointers are created during global equivalence checks */
	private SearchNode pointer = null;

	/** The node's depth */
	private int depth = 0;

	private Metadata metadata = null;

	/** True if the node is fully generated */
	private Boolean isFullyGenerated = false;
	
	/** The path from root */
	private final List<Integer> pathFromRoot;

	/** The best path from root */
	private List<Integer> bestPathFromRoot = null;

	/** The path plan from root */
	private LeftDeepPlan bestPlanFromRoot = null;

	/** The plan that cost dominates the node */
	private LeftDeepPlan dominancePlan = null;

	/**
	 * @param configuration The configuration of the node
	 * @throws PlannerException
	 */
	public SearchNode(LinearChaseConfiguration configuration) throws PlannerException {
		this.id = globalId++;
		this.configuration = configuration;
		this.pathFromRoot = null;
	}

	/**
	 * @param parent The parent node
	 * @param configuration The configuration of the node
	 * @throws PlannerException
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
	 * Searches for query matches in the current configuration
	 * @return the list of matches
	 * @throws PlannerException
	 */
	public List<Match> matchesQuery(Query<?> query) throws PlannerException, LimitReachedException {
		return this.configuration.matchesQuery(query);
	}

	/**
	 * Closes the configuration of this node
	 * @throws PlannerException
	 */
	public void close(Chaser chaser, Query<?> query, Collection<? extends Constraint> dependencies) throws PlannerException, LimitReachedException {
		this.configuration.reasonUntilTermination(chaser, query, dependencies);
		this.isFullyGenerated = true;
	}

	/**
	 * @param parentNode
	 * @return true if the current node is globally dominated by the input one
	 */
	public boolean isDominatedBy(SearchNode target) {
		return new Dominance().isDominated(this, target);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}

	public LinearChaseConfiguration getConfiguration() {
		return this.configuration;
	}

	public void setStatus(NodeStatus s) {
		this.status = s;
	}

	public NodeStatus getStatus() {
		return this.status;
	}

	/**
	 * @param pointer The pointer node 
	 */
	public void setPointer(SearchNode pointer) {
		this.pointer = pointer;
	}

	public SearchNode getPointer() {
		return this.pointer;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getDepth() {
		return this.depth;
	}

	public int getId() {
		return this.id;
	}
	/**
	 * 
	 * @return true if the configuration is fully closed
	 */
	public Boolean isFullyGenerated() {
		return this.isFullyGenerated;
	}


	public void setIsFullyGenerated(Boolean isFullyGenerated) {
		this.isFullyGenerated = isFullyGenerated;
	}

	public Metadata getMetadata() {
		return this.metadata;
	}


	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}
	
	public List<Integer> getBestPathFromRoot() {
		return this.bestPathFromRoot;
	}

	/**
	 * @return List<Integer>
	 */
	public List<Integer> getPathFromRoot() {
		return this.pathFromRoot;
	}

	/**
	 * @return LinearPlan
	 */
	public LeftDeepPlan getBestPlanFromRoot() {
		return this.bestPlanFromRoot;
	}

	/**
	 * @param pathFromRoot List<Integer>
	 */
	public void setBestPathFromRoot(List<Integer> pathFromRoot) {
		this.bestPathFromRoot = pathFromRoot;
	}

	/**
	 * @param planFromRoot LinearPlan
	 */
	public void setBestPlanFromRoot(LeftDeepPlan planFromRoot) {
		this.bestPlanFromRoot = planFromRoot;
	}

	/**
	 * @return LinearPlan
	 */
	public LeftDeepPlan getDominancePlan() {
		return this.dominancePlan;
	}

	/**
	 * @param dominancePlan LinearPlan
	 */
	public void setDominancePlan(LeftDeepPlan dominancePlan) {
		this.dominancePlan = dominancePlan;
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		return String.valueOf(this.id);
	}

	/**
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
	 * @param pathToSuccess List<Integer>
	 */
	public abstract void setPathToSuccess(List<Integer> pathToSuccess);

	/**
	 *
	 * @author Efthymia Tsamoura
	 *
	 */
	private class Dominance {
		private final FactDominance factDominance = new FastFactDominance(false);

		/**
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
