package uk.ac.ox.cs.pdq.planner.linear.explorer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.OrderDependentCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.OrderIndependentCostEstimator;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.equivalence.linear.LinearEquivalenceClasses;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.LinearConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.cost.CostPropagator;
import uk.ac.ox.cs.pdq.planner.linear.cost.OrderDependentCostPropagator;
import uk.ac.ox.cs.pdq.planner.linear.cost.OrderIndependentCostPropagator;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode.NodeStatus;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.util.LimitReachedException;

/**
 * Searches the proof space employing several optimisations heuristics in order
 * to reach faster the best plan.
 *
 * @author Efthymia Tsamoura
 * @author Gabor
 *
 */
public class LinearOptimized extends LinearExplorer {

	private static Logger log = Logger.getLogger(LinearOptimized.class);

	/** Propagates to the root of the plan tree the best plan found so far. */
	@SuppressWarnings("rawtypes")
	protected final CostPropagator costPropagator;

	/** How many rounds we do not check for query matches. */
	protected final int queryMatchInterval;

	/** Performs plan post-pruning. */
	private final PostPruningRemoveFollowUps postPruning;

	/** TOCOMMENT: WHAT IS IT . */
	private final Set<List<Integer>> prunedPaths = new HashSet<>();

	/** Classes of equivalent configurations. */
	private LinearEquivalenceClasses equivalenceClasses = new LinearEquivalenceClasses();

	/** The unexplored descendants. */
	private final Queue<SearchNode> unexploredDescendants = new PriorityQueue<>(10, new Comparator<SearchNode>() {
		@Override
		public int compare(SearchNode o1, SearchNode o2) {
			if (o1.getId() >= o2.getId())
				return -1;
			return 1;
		}
	});

	/**
	 * Instantiates a new linear optimized.
	 *
	 * @param eventBus
	 *            the event bus
	 * @param collectStats
	 *            the collect stats
	 * @param query
	 *            The input user query
	 * @param accessibleQuery
	 *            The accessible counterpart of the user query
	 * @param schema
	 *            The input schema
	 * @param accessibleSchema
	 *            The accessible counterpart of the input schema
	 * @param chaser
	 *            Runs the chase algorithm
	 * @param detector
	 *            Detects homomorphisms during chasing
	 * @param costEstimator
	 *            Estimates the cost of a plan
	 * @param nodeFactory
	 *            the node factory
	 * @param depth
	 *            the depth
	 * @param queryMatchInterval
	 *            the query match interval
	 * @param postPruning
	 *            Removes the redundant follow up joins and accesses from a plan
	 * @throws PlannerException
	 *             the planner exception
	 * @throws SQLException
	 */
	@SuppressWarnings("rawtypes")
	public LinearOptimized(EventBus eventBus, ConjunctiveQuery query, AccessibleSchema accessibleSchema, Chaser chaser,
			DatabaseManager connection, CostEstimator costEstimator, CostPropagator costPropagator, int depth,
			int queryMatchInterval) throws PlannerException, SQLException {
		super(eventBus, query, accessibleSchema, chaser, connection, costEstimator, depth);
		Preconditions.checkNotNull(costPropagator);
		Preconditions.checkArgument(queryMatchInterval >= 0);
		Preconditions.checkArgument((costPropagator instanceof OrderIndependentCostPropagator
				&& costEstimator instanceof OrderIndependentCostEstimator)
				|| (costPropagator instanceof OrderDependentCostPropagator
						&& costEstimator instanceof OrderDependentCostEstimator));
		this.costPropagator = costPropagator;
		this.queryMatchInterval = queryMatchInterval;
		this.postPruning = new PostPruningRemoveFollowUps(accessibleSchema, chaser, this.accessibleQuery);
		// initalize equivalence classes with the initial applyrules.
		for (SearchNode node:this.planTree.vertexSet()) {
			equivalenceClasses.add(node);
		}
		
	}

	/**
	 * 
	 *
	 * @throws PlannerException
	 *             the planner exception
	 * @throws LimitReachedException
	 *             the limit reached exception
	 */
	@Override
	public void performSingleExplorationStep() throws PlannerException, LimitReachedException {
		_performSingleExplorationStep();
	}

	public SearchNode _performSingleExplorationStep() throws PlannerException, LimitReachedException {
		SearchNode freshNode = null;
		if (this.unexploredDescendants.isEmpty()) {
			// Choose the next node to explore below it
			SearchNode selectedNode = this.chooseNode();
			if (selectedNode == null)
				return null;
			freshNode = this.explorationStep(selectedNode);
			
		} else {
			SearchNode selectedNode = this.unexploredDescendants.peek();
			if (selectedNode == null)
				return null;
			freshNode = this.explorationStep(selectedNode);
			if (freshNode.getStatus().equals(NodeStatus.ONGOING))
				this.unexploredDescendants.add(freshNode);
			this.unexploredDescendants.remove(selectedNode);
		}
		this.rounds++;
		return freshNode;
	}

	/**
	 * Exploration step.
	 *
	 * @param selectedNode
	 *            Node to expand
	 * @return the newly created node that is added below the input one in the plan
	 *         tree
	 * @throws PlannerException
	 *             the planner exception
	 * @throws LimitReachedException
	 *             the limit reached exception
	 */
	public SearchNode explorationStep(SearchNode selectedNode) throws PlannerException, LimitReachedException {
		LinearConfiguration selectedConfig = selectedNode.getConfiguration();
		/*
		 * Choose a new candidate fact. A candidate fact F(c1,c2,...,cN) is one for
		 * which (i) there exists Accessible(c_i) facts for any c_i (ii)
		 * AccessedF(c1,c2,...,cN) does not exist in the current initialConfig
		 */
		Candidate selectedCandidate = selectedConfig.chooseCandidate();
		if (selectedCandidate == null) {
			selectedNode.setStatus(NodeStatus.TERMINAL);
			return null;
		}
		// Search for other candidate facts that could be exposed along with the
		// selected candidate.
		Set<Candidate> similarCandidates = selectedConfig.getSimilarCandidates(selectedCandidate);
		selectedConfig.removeCandidates(similarCandidates);
		return this.explorationStep(selectedNode, selectedCandidate,similarCandidates);
	}
	
	/** 
	 * Same as the explorationStep function above, but the parameters are broke down to support testing better.
	 */
	public SearchNode explorationStep(SearchNode selectedNode,Candidate selectedCandidate, Set<Candidate> similarCandidates) throws PlannerException, LimitReachedException {
		LinearConfiguration selectedConfig = selectedNode.getConfiguration();

		if (!selectedConfig.hasCandidates()) {
			selectedNode.setStatus(NodeStatus.TERMINAL);
		}

		// Create a new node from the exposed facts and add it to the plan tree
		LinearChaseConfiguration newConfiguration = null;
		
		SearchNode freshNode = null;
		boolean executeChase = true;
		SearchNode selectedNodesRepresentative = equivalenceClasses.searchRepresentative(selectedNode);
		List<SearchNode> wholeClass = new ArrayList<>();
		if (selectedNodesRepresentative !=null)
			wholeClass.addAll(equivalenceClasses.getEquivalenceClass(selectedNodesRepresentative));
		// when there are equivalent classes, we can check if we have the selected candidate already exposed in one of them.
		SearchNode nodeToClone = null;
		for (SearchNode sn : wholeClass) {
			if (executeChase && sn.getConfiguration().getExposedCandidates()!=null) {
				for (Candidate c:getExposedCandidatesOf(sn)) {
					if (c.isEqualAxiom(selectedCandidate)) {
						//the selected candidate was already exposed in sn, so we can copy the facts
						nodeToClone = getExposedByCandidates(sn,c);
						newConfiguration = new LinearChaseConfiguration(selectedNode.getConfiguration(),similarCandidates,nodeToClone.getConfiguration().getState().clone());
						executeChase=false;
						freshNode = new LinearConfigurationNode((LinearConfigurationNode) selectedNode, newConfiguration);
						this.equivalenceClasses.add(this.equivalenceClasses.searchRepresentative(nodeToClone), freshNode);
						break;
					}
				}
			}
		}
		if (executeChase) {
			// when we havent found a node to clone, we need to create a fresh new node. 
			newConfiguration = new LinearChaseConfiguration(selectedNode.getConfiguration(),similarCandidates);
			freshNode = new LinearConfigurationNode((LinearConfigurationNode) selectedNode, newConfiguration);
		}
		freshNode.getConfiguration().detectCandidates(this.accessibleSchema);
		if (!freshNode.getConfiguration().hasCandidates())
			freshNode.setStatus(NodeStatus.TERMINAL);
		Cost cost = this.costEstimator.cost(freshNode.getConfiguration().getPlan());
		freshNode.getConfiguration().setCost(cost);
		freshNode.setCostOfBestPlanFromRoot(cost);

		log.info("SELECTED NODE: " + selectedNode);
		log.info("EXPOSED CANDIDATES\t");
		if (selectedNode.getConfiguration().getExposedCandidates() != null) {
			log.info(Joiner.on("\n\t").join(selectedNode.getConfiguration().getExposedCandidates()));
		}
		log.info("UNEXPOSED CANDIDATES\t");
		log.info(Joiner.on("\n\t").join(selectedNode.getConfiguration().getCandidates()));
		log.info("FRESH: " + freshNode + " PARENT: " + selectedNode);
		log.info("EXPOSED CANDIDATES\t");
		log.info(Joiner.on("\n\t").join(freshNode.getConfiguration().getExposedCandidates()));
		log.info("UNEXPOSED CANDIDATES\t");
		log.info(Joiner.on("\n\t").join(freshNode.getConfiguration().getCandidates()));

		this.planTree.addVertex(freshNode);
		this.planTree.addEdge(selectedNode, freshNode, new DefaultEdge());

		boolean dominated = false;
		if (this.bestPlan != null) {
			if (freshNode.getCostOfBestPlanFromRoot().greaterOrEquals(this.bestCost)) {
				dominated = true;
				freshNode.setDominatingPlan(this.bestPlan);
				freshNode.setCostOfDominatingPlan(this.bestCost);
				log.debug(freshNode.getBestPlanFromRoot() + " has higher cost than plan " + this.bestPlan + " Costs "
						+ freshNode.getCostOfBestPlanFromRoot() + ">=" + this.bestCost);
			}
		}

		// set dominating plan if there is one
		if (!dominated && this.costPropagator instanceof OrderIndependentCostPropagator) {
			SearchNode dominatingNode = SearchNode.isCostAndFactDominated(this.planTree.vertexSet(), freshNode);
			if (dominatingNode != null) {
				dominated = true;
				freshNode.setDominatingPlan(dominatingNode.getConfiguration().getPlan());
				freshNode.setCostOfDominatingPlan(dominatingNode.getConfiguration().getCost());
				log.debug(dominatingNode.getConfiguration().getPlan() + " dominates "
						+ freshNode.getCostOfBestPlanFromRoot() + dominatingNode.getConfiguration().getCost() + "<"
						+ freshNode.getCostOfBestPlanFromRoot());
			}
		}
		
		if (!dominated) {
			if (executeChase) { 
				// If we need to execute chase, we can do so by calling the close method on the node.
				freshNode.close(this.chaser, this.accessibleSchema.getInferredAccessibilityAxioms());
				this.equivalenceClasses.add(freshNode);
			}
			
			/* Check for query match */
			if (this.rounds % this.queryMatchInterval == 0) {
				List<Match> matches = freshNode.matchesQuery(this.accessibleQuery);

				// If there exists at least one query match
				if (!matches.isEmpty()) {
					freshNode.setStatus(NodeStatus.SUCCESSFUL);
					this.updateBestPlan(selectedNode, freshNode, matches.get(0));
				}
			}
		} else {
			// dominated node should be closed.
			freshNode.setStatus(NodeStatus.TERMINAL);
			this.eventBus.post(freshNode);
		}
		
		return freshNode;
	}

	/** Gets the exposed candidates from the node and its immediate children
	 * @param sn
	 * @return
	 */
	private Set<Candidate> getExposedCandidatesOf(SearchNode sn) {
		Set<Candidate> candidates = new HashSet<>();
		candidates.addAll(sn.getConfiguration().getExposedCandidates());
		for (SearchNode child:planTree.getChildren(sn)) {
			candidates.addAll(child.getConfiguration().getExposedCandidates());
		}
		return candidates;
	}
	
	/** Gets the searchnode that exposed the given candidates.
	 * @param sn
	 * @return
	 */
	private SearchNode getExposedByCandidates(SearchNode sn, Candidate c) {
		if (sn.getConfiguration().getExposedCandidates().contains(c))
			return sn;
		for (SearchNode child:planTree.getChildren(sn)) {
			if (child.getConfiguration().getExposedCandidates().contains(c))
				return child;
		}
		return null;
	}

	/**
	 * Update best plan.
	 *
	 * @param parentNode
	 *            the parent node
	 * @param freshNode
	 *            the fresh node
	 * @param match
	 *            the match
	 * @throws PlannerException
	 *             the planner exception
	 * @throws LimitReachedException
	 *             the limit reached exception
	 */
	@SuppressWarnings("unchecked")
	private void updateBestPlan(SearchNode parentNode, SearchNode freshNode, Match match)
			throws PlannerException, LimitReachedException {
		SearchNode nodeToAdd = freshNode;

		// compute best path for new node
		List<Integer> newPath = nodeToAdd.getPathFromRoot();
		
		// check if we need to prune this new node.
		if (this.postPruning != null && !this.prunedPaths.contains(newPath)) {
			this.prunedPaths.add(newPath);
			List<SearchNode> path = this.planTree.createPath(newPath);
			Atom[] factsInQueryMatch = uk.ac.ox.cs.pdq.fol.Formula
					.applySubstitution(this.accessibleQuery, match.getMapping()).getAtoms();
			boolean isPruned = this.postPruning.pruneSearchNodePath(this.planTree.getRoot(), path, factsInQueryMatch);
			if (isPruned) {
				this.postPruning.addPrunedPathToTree(this.planTree, this.planTree.getRoot(),
						this.postPruning.getPath());
				this.prunedPaths.add(newPath);
				// update the nodeToAdd with the new pruned node.
				nodeToAdd = this.postPruning.getPath().get(this.postPruning.getPath().size() - 1);
			}
		}
		// add new node to cost propagator (this will re-calculate the best plan and its
		// cost if needed)
		this.costPropagator.propagate(nodeToAdd, this.planTree);
		// get new best plan and its cost.
		RelationalTerm successfulPlan = this.costPropagator.getBestPlan();
		Cost costOfSuccessfulPlan = this.costPropagator.getBestCost();

		// check if best plan changed
		if (this.bestPlan == null && successfulPlan != null
				|| this.bestPlan != null && successfulPlan != null && costOfSuccessfulPlan.lessThan(this.bestCost)) {
			// update best plan.
			this.bestPlan = successfulPlan;
			this.bestCost = costOfSuccessfulPlan;
			this.eventBus.post(this.getBestPlan());
			this.eventBus.post(this.getBestPlan());
			log.trace("\t+++BEST PLAN: " + this.bestPlan.getAccesses() + " " + this.bestCost);
		}
	}
	
	/** For testing purposes we can return the inner field LinearEquivalenceClasses
	 * @return
	 */
	public LinearEquivalenceClasses getLinearEquivalenceClasses() {
		return equivalenceClasses;
	}
	/**
	 * @return postPruning
	 */
	public PostPruningRemoveFollowUps getPostPruning() {
		return postPruning;
	}
}
