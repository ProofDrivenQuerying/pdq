package uk.ac.ox.cs.pdq.planner.linear.explorer;

import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.DOMINANCE_PRUNING;
import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.EQUIVALENCE_PRUNING;
import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.HIGHER_COST_PRUNING;
import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.MILLI_CLOSE;
import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.MILLI_DOMINANCE;
import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.MILLI_EQUIVALENCE;
import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.MILLI_QUERY_MATCH;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;

import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.accessible.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.linear.LinearConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.LinearUtility;
import uk.ac.ox.cs.pdq.planner.linear.cost.BlackBoxPropagator;
import uk.ac.ox.cs.pdq.planner.linear.cost.CostPropagator;
import uk.ac.ox.cs.pdq.planner.linear.cost.PropagatorUtils;
import uk.ac.ox.cs.pdq.planner.linear.cost.SimplePropagator;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.NodeFactory;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode.NodeStatus;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.equivalence.PathEquivalenceClasses;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.equivalence.PathEquivalenceClasses.PathEquivalenceClass;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.BestPlanMetadata;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.CreationMetadata;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.DominanceMetadata;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.EquivalenceMetadata;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.Metadata;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.StatusUpdateMetadata;
import uk.ac.ox.cs.pdq.planner.linear.explorer.pruning.PostPruning;
import uk.ac.ox.cs.pdq.reasoning.Match;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;
import uk.ac.ox.cs.pdq.util.IndexedDirectedGraph;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

/**
 * Searches the proof space employing several optimisations heuristics
 * in order to reach faster the best plan.
 * For more information see
 * "Michael Benedikt, Balder ten Cate, Efthymia Tsamoura. Generating Low-cost Plans From Proofs"
 *
 * @author Efthymia Tsamoura
 *
 */
public class LinearOptimized extends LinearExplorer {

	/** Logger. */
	private static Logger log = Logger.getLogger(LinearOptimized.class);

	/** Propagates to the root of the plan tree the best plan found so far */
	protected final CostPropagator costPropagator;

	/** How often we check for query matches */
	protected final int queryMatchInterval;

	/** Performs plan post-pruning */
	private final PostPruning postPruning;
	private final Set<List<Integer>> prunedPaths = new HashSet<>(); 

	private final boolean zombification;

	/** Classes of equivalent configurations */
	private PathEquivalenceClasses equivalenceClasses = new PathEquivalenceClasses();

	private final Queue<SearchNode> unexploredDescendants = new PriorityQueue<>(10, new Comparator<SearchNode>() {
		@Override
		public int compare(SearchNode o1, SearchNode o2) {
			if(o1.getId() >= o2.getId()) {
				return -1;
			}
			return 1;
		}
	});


	/**
	 * 
	 * @param eventBus
	 * @param collectStats
	 * @param query
	 * 		The input user query
	 * @param accessibleQuery
	 * 		The accessible counterpart of the user query
	 * @param schema
	 * 		The input schema
	 * @param accessibleSchema
	 * 		The accessible counterpart of the input schema
	 * @param chaser
	 * 		Runs the chase algorithm
	 * @param detector
	 * 		Detects homomorphisms during chasing
	 * @param costEstimator
	 * 		Estimates the cost of a plan
	 * @param nodeFactory
	 * @param depth
	 * @param queryMatchInterval
	 * @param postPruning
	 * 		Removes the redundant follow up joins and accesses from a plan
	 * @param zombification
	 * 		True if we wake up previously terminal nodes
	 * @throws PlannerException
	 */
	public LinearOptimized(
			EventBus eventBus, 
			boolean collectStats,
			Query<?> query,
			Query<?> accessibleQuery,
			Schema schema,
			AccessibleSchema accessibleSchema, 
			Chaser chaser,
			HomomorphismDetector detector,
			CostEstimator<LeftDeepPlan> costEstimator,
			NodeFactory nodeFactory,
			int depth,
			int queryMatchInterval, 
			PostPruning postPruning,
			boolean zombification) throws PlannerException {
		super(eventBus, collectStats, query, accessibleQuery, schema, accessibleSchema, chaser, detector, costEstimator, nodeFactory, depth);
		this.costPropagator = PropagatorUtils.getPropagator(costEstimator);
		this.queryMatchInterval = queryMatchInterval;
		this.postPruning = postPruning;
		this.zombification = zombification;
	}

	/**
	 * @throws PlannerException
	 */
	@Override
	protected void _explore() throws PlannerException, LimitReachedException {
		if(this.unexploredDescendants.isEmpty()) {
			// Choose the next node to explore below it
			SearchNode selectedNode = this.chooseNode();
			if (selectedNode == null) {
				return;
			}
			this.explorationStep(selectedNode);
		}
		else {
			SearchNode selectedNode = this.unexploredDescendants.peek();
			if (selectedNode == null) {
				return;
			}
			SearchNode freshNode = this.explorationStep(selectedNode);
			if(freshNode.getStatus().equals(NodeStatus.ONGOING)) {
				this.unexploredDescendants.add(freshNode);
			}
			this.unexploredDescendants.remove(selectedNode);
		}
	}

	/**
	 * @param selectedNode Node to expand
	 * @return the newly created node that is added below the input one in the plan tree
	 * @throws PlannerException
	 */
	protected SearchNode explorationStep(SearchNode selectedNode) throws PlannerException, LimitReachedException  {

		LinearConfiguration selectedConfig = selectedNode.getConfiguration();

		/*
		 * Choose a new candidate fact. A candidate fact F(c1,c2,...,cN) is one for which
		 * (i) there exists Accessible(c_i) facts for any c_i
		 * (ii) AccessedF(c1,c2,...,cN) does not exist in the current initialConfig
		 */
		Candidate selectedCandidate = selectedConfig.chooseCandidate();
		if(selectedCandidate == null) {
			selectedNode.setStatus(NodeStatus.TERMINAL);
			Metadata metadata = new StatusUpdateMetadata(selectedNode, this.getElapsedTime());
			selectedNode.setMetadata(metadata);
			this.eventBus.post(selectedNode);
			return null;
		}

		// Search for other candidate facts that could be exposed along with the selected candidate. 
		Set<Candidate> similarCandidates = selectedConfig.getSimilarCandidates(selectedCandidate);
		selectedConfig.removeCandidates(similarCandidates);
		if (!selectedConfig.hasCandidates()) {
			selectedNode.setStatus(NodeStatus.TERMINAL);
		}

		// Create a new node from the exposed facts and add it to the plan tree
		SearchNode freshNode = this.getNodeFactory().getInstance(selectedNode, similarCandidates);	
		freshNode.getConfiguration().detectCandidates(this.accessibleSchema);
		if (!freshNode.getConfiguration().hasCandidates()) {
			freshNode.setStatus(NodeStatus.TERMINAL);
		}
		this.costEstimator.cost(freshNode.getConfiguration().getPlan());

		log.info("SELECTED NODE: " + selectedNode);
		log.info("EXPOSED CANDIDATES\t");
		if(selectedNode.getConfiguration().getExposedCandidates() != null) {
			log.info(Joiner.on("\n\t").join(selectedNode.getConfiguration().getExposedCandidates()));
		}
		log.info("UNEXPOSED CANDIDATES\t");
		log.info(Joiner.on("\n\t").join(selectedNode.getConfiguration().getCandidates()));
		log.info("FRESH: " + freshNode + " PARENT: " + selectedNode);
		log.info("EXPOSED CANDIDATES\t");
		log.info(Joiner.on("\n\t").join(freshNode.getConfiguration().getExposedCandidates()));
		log.info("UNEXPOSED CANDIDATES\t");
		log.info(Joiner.on("\n\t").join(freshNode.getConfiguration().getCandidates()));


		Metadata metadata = new CreationMetadata(selectedNode, this.getElapsedTime());
		freshNode.setMetadata(metadata);
		this.eventBus.post(freshNode);

		this.planTree.addVertex(freshNode);
		this.planTree.addEdge(selectedNode, freshNode, new DefaultEdge());

		// If the cost of the plan of the newly created node is higher than the best plan found so far 
		//then zombify the newly created node  
		boolean domination = false;
		LeftDeepPlan freshNodePlan = freshNode.getBestPlanFromRoot();
		if (this.bestPlan != null) {
			if (freshNodePlan.getCost().greaterOrEquals(this.bestPlan.getCost())) {
				domination = true;
				freshNode.setDominancePlan(this.bestPlan);
				metadata = new DominanceMetadata(selectedNode, this.bestPlan, freshNodePlan, this.getElapsedTime());
				freshNode.setMetadata(metadata);
				this.stats.increase(HIGHER_COST_PRUNING, 1);
				log.debug(freshNodePlan.toString() + " has higher cost than plan " + this.bestPlan.toString() + " Costs " +  freshNodePlan.getCost().toString() + ">=" + this.bestPlan.getCost().toString());
			}
		}

		// If at least one node in the plan tree dominates the newly created node, then zombify the newly created node
		if (!domination && this.costPropagator instanceof SimplePropagator) {
			this.stats.start(MILLI_DOMINANCE);
			SearchNode dominanceNode = ExplorerUtils.isDominated(this.planTree.vertexSet(), freshNode);
			this.stats.stop(MILLI_DOMINANCE);
			if(dominanceNode != null) {
				domination = true;
				LeftDeepPlan dominancePlan = dominanceNode.getConfiguration().getPlan();
				freshNode.setDominancePlan(dominancePlan);
				metadata = new DominanceMetadata(dominanceNode, dominancePlan, freshNodePlan, this.getElapsedTime());
				freshNode.setMetadata(metadata);
				this.stats.increase(DOMINANCE_PRUNING, 1);
				log.debug(dominancePlan.toString() + " dominates " + freshNodePlan.toString() + dominancePlan.getCost().toString() + "<" + freshNodePlan.getCost().toString());

			}
		}

		// Close the newly created node using the inferred accessible dependencies of the accessible schema
		this.stats.start(MILLI_CLOSE);
		freshNode.close(this.chaser, this.accessibleQuery, this.accessibleSchema.getInferredAccessibilityAxioms());
		this.stats.stop(MILLI_CLOSE);

		if (domination) {
			freshNode.setStatus(NodeStatus.TERMINAL);
			this.eventBus.post(freshNode);

		} else {
			// Find a node that is globally equivalent to the newly created node.
			this.stats.start(MILLI_EQUIVALENCE);
			SearchNode parentEquivalent = ExplorerUtils.isEquivalent(ExplorerUtils.allButAncestorsOf(this.planTree, freshNode), freshNode);
			this.stats.stop(MILLI_EQUIVALENCE);

			/*
			 * If such a node exists then
			 * -create a pointer from the newly created node to the one that is globally equivalent to
			 * -propagate upwards the best paths to success
			 * -update the best plan found so far
			 * -stop exploring plans below the newly created node
			 */
			if (parentEquivalent != null && !parentEquivalent.getStatus().equals(NodeStatus.SUCCESSFUL)) {
				freshNode.setPointer(parentEquivalent);
				if(this.zombification) {
					PathEquivalenceClass equivalenceClass = this.equivalenceClasses.addEntry(parentEquivalent, freshNode);
					if (this.costPropagator instanceof BlackBoxPropagator) {
						this.wakeupDescendants(freshNode.getPathFromRoot(), equivalenceClass);
					}
				}
				freshNode.setStatus(NodeStatus.TERMINAL);
				metadata = new EquivalenceMetadata(selectedNode, this.getElapsedTime());
				freshNode.setMetadata(metadata);
				this.eventBus.post(freshNode);
				//Cannot do postpruning here
				this.updateBestPlan(selectedNode, freshNode);
				this.stats.increase(EQUIVALENCE_PRUNING, 1);
			}

			//If the newly created node is not equivalent to any node in the plan tree then create a new equivalence class
			else{
				this.equivalenceClasses.addEntry(freshNode);
				/* Check for query match */
				if (this.rounds % this.queryMatchInterval == 0) {
					this.stats.start(MILLI_QUERY_MATCH);
					List<Match> matches = freshNode.matchesQuery(this.accessibleQuery);
					this.stats.stop(MILLI_QUERY_MATCH);

					// If there exists at least one query match
					if (!matches.isEmpty()) {
						freshNode.setStatus(NodeStatus.SUCCESSFUL);
						//TODO post this event?
						this.updateBestPlan(selectedNode, freshNode, matches.get(0));
					}
				}
			}
		}
		return freshNode;
	}

	private void updateBestPlan(SearchNode parentNode, SearchNode freshNode, Match match) throws PlannerException, LimitReachedException {
		this.costPropagator.propagate(freshNode, this.planTree);
		LeftDeepPlan successfulPlan = this.costPropagator.getBestPlan();
		if ((this.bestPlan == null && successfulPlan != null) || 
				(this.bestPlan != null && successfulPlan != null && successfulPlan.getCost().lessThan(this.bestPlan.getCost()))) {
			this.bestPlan = successfulPlan;
			this.bestConfigurationsList = this.getConfigurations(this.costPropagator.getBestPath());
			this.eventBus.post(this.getBestPlan());
		
			if(this.postPruning != null && !this.prunedPaths.contains(this.costPropagator.getBestPath())) {
				this.prunedPaths.add(this.costPropagator.getBestPath());
				List<SearchNode> path = LinearUtility.createPath(this.planTree, this.costPropagator.getBestPath());
				List<Predicate> queryFacts = this.accessibleQuery.ground(match.getMapping()).getPredicates();
				boolean isPruned = this.postPruning.prune(this.planTree.getRoot(), path, queryFacts);
				if(isPruned) {
					this.postPruning.addPrunedPathToTree(this.planTree, this.planTree.getRoot(), this.postPruning.getPath());
					freshNode = this.postPruning.getPath().get( this.postPruning.getPath().size()-1);
					this.costPropagator.propagate(freshNode, this.planTree);
					successfulPlan = this.costPropagator.getBestPlan();
					if ((this.bestPlan == null && successfulPlan != null) || 
							(this.bestPlan != null && successfulPlan != null && successfulPlan.getCost().lessThan(this.bestPlan.getCost()))) {
						this.bestPlan = successfulPlan;
						this.bestConfigurationsList = this.getConfigurations(this.costPropagator.getBestPath());
						this.eventBus.post(this.getBestPlan());
					}
					this.prunedPaths.add(this.costPropagator.getBestPath());
				}
			}
			log.trace("\t+++BEST PLAN: " + this.bestPlan.getAccesses() + " " + this.bestPlan.getCost());

			BestPlanMetadata successMetadata = new BestPlanMetadata(parentNode, this.bestPlan, this.costPropagator.getBestPath(), 
					this.bestConfigurationsList, this.getElapsedTime());
			freshNode.setMetadata(successMetadata);
			this.eventBus.post(freshNode);
		}
	}
	
	
	private void updateBestPlan(SearchNode parentNode, SearchNode freshNode) throws PlannerException, LimitReachedException {
		this.costPropagator.propagate(freshNode, this.planTree);
		LeftDeepPlan successfulPlan = this.costPropagator.getBestPlan();
		if ((this.bestPlan == null && successfulPlan != null) 
			|| (this.bestPlan != null && successfulPlan != null 
				&& successfulPlan.getCost().lessThan(this.bestPlan.getCost()))) {
			this.bestPlan = successfulPlan;
			this.bestConfigurationsList = this.getConfigurations(this.costPropagator.getBestPath());
			this.eventBus.post(this.getBestPlan());
			log.trace("\t+++BEST PLAN: " + this.bestPlan.getAccesses() + " " + this.bestPlan.getCost());

			BestPlanMetadata successMetadata = new BestPlanMetadata(parentNode, this.bestPlan, this.costPropagator.getBestPath(), 
					this.bestConfigurationsList, this.getElapsedTime());
			freshNode.setMetadata(successMetadata);
			this.eventBus.post(freshNode);
		}
	}

	/**
	 * Wakes up (de-zombifies) the unexplored descendants of a path.
	 * A path b_1, b_2, ..., b_j, b_{j+1} is zombified when it is (success) dominated.
	 * However, if a new path a_1, a_2, ..., a_ i is found which is equivalent to the prefix path b_1, b_2, ..., b_j and
	 * the cost of the plan built up from the path a_1, a_2, ..., a_i, b_{j+1} is lower than the best plan found so far
	 * then we wake up the zombie path
	 *
	 * @param path
	 * @param equivalenceClass
	 * @throws PlannerException
	 */
	private void wakeupDescendants(List<Integer> path, PathEquivalenceClass equivalenceClass) 
			throws PlannerException, LimitReachedException  {
		this.wakeupDescendants(path, equivalenceClass, Sets.<List<Integer>>newHashSet());
	}

	/**
	 * @param path List<Integer>
	 * @param equivalenceClass EquivalenceClass
	 * @param visitedPaths Set<List<Integer>>
	 * @throws PlannerException
	 */
	private void wakeupDescendants(List<Integer> path, PathEquivalenceClass equivalenceClass, 
			Set<List<Integer>> visitedPaths)
					throws PlannerException, LimitReachedException  {
		List<Integer> representativePath = equivalenceClass.getRepresentativePath();
		Set<SearchNode> deadDescendants = this.getDeadDescendants(equivalenceClass.getRepresentativeNode(), this.planTree);
		for(SearchNode deadDescendant:deadDescendants) {

			List<Integer> pathFromRoot = deadDescendant.getPathFromRoot();
			List<Integer> equivalencePath = this.createPath(representativePath, path, pathFromRoot);
			LeftDeepPlan equivalencePlan = PropagatorUtils.createLeftDeepPlan(this.planTree, equivalencePath, this.costPropagator.getCostEstimator());

			if(equivalencePlan.getCost().lessThan(deadDescendant.getBestPlanFromRoot().getCost())) {
				deadDescendant.setBestPathFromRoot(equivalencePath);
				deadDescendant.setBestPlanFromRoot(equivalencePlan);
			}

			if((this.bestPlan == null && equivalencePlan.getCost().lessThan(deadDescendant.getDominancePlan().getCost())) ||
					(this.bestPlan != null && equivalencePlan.getCost().lessThan(this.bestPlan.getCost()))	) {

				this.stats.start(MILLI_QUERY_MATCH);
				List<Match> matches = deadDescendant.matchesQuery(this.accessibleQuery);
				this.stats.stop(MILLI_QUERY_MATCH);

				SearchNode parent = this.planTree.getParent(deadDescendant);
				if (!matches.isEmpty()) {
					deadDescendant.setStatus(NodeStatus.SUCCESSFUL);
					this.equivalenceClasses.addEntry(deadDescendant);
					this.updateBestPlan(parent, deadDescendant, matches.get(0));
				}
				else {
					deadDescendant.setStatus(NodeStatus.ONGOING);
					deadDescendant.setDominancePlan(null);
					this.equivalenceClasses.addEntry(deadDescendant);
					this.unexploredDescendants.add(deadDescendant);
				}
			}
		}

		Set<List<Integer>> otherPaths = equivalenceClass.getPaths();
		for(List<Integer> otherPath:otherPaths) {
			if(!visitedPaths.contains(otherPath)) {
				visitedPaths.add(otherPath);
				List<Entry<PathEquivalenceClass, List<Integer>>> isPrefixOf = this.equivalenceClasses.isPrefixOf(otherPath);
				for(Entry<PathEquivalenceClass, List<Integer>> entry:isPrefixOf) {
					if(!entry.getKey().equals(equivalenceClass)) {
						List<Integer> existingPath = entry.getValue();
						List<Integer> outputPath = this.createPath(otherPath, path, existingPath);
						this.equivalenceClasses.addEntry(outputPath, entry.getKey());
						this.wakeupDescendants(outputPath, entry.getKey(), visitedPaths);
					}
				}
			}
		}

	}

	/**
	 * @param target List<Integer>
	 * @param replacement List<Integer>
	 * @param source List<Integer>
	 * @return List<Integer>
	 */
	private List<Integer> createPath(List<Integer> target, List<Integer> replacement, List<Integer> source) {
		Preconditions.checkArgument(Collections.indexOfSubList(source, target) == 0);
		List<Integer> output = Lists.newArrayList(replacement);
		output.addAll(source.subList(target.size(), source.size()));
		return output;
	}

	/**
	 * @param representativeNode N
	 * @param planTree IndexedDirectedGraph<N>
	 * @return Set<N>
	 */
	private Set<SearchNode> getDeadDescendants(SearchNode representativeNode, IndexedDirectedGraph<SearchNode> planTree) {
		Set<SearchNode> deadDescendants = Sets.newHashSet();
		this.getDeadDescendantsRecursive(representativeNode, planTree, deadDescendants);
		return deadDescendants;
	}

	/**
	 * @param representativeNode N
	 * @param planTree IndexedDirectedGraph<N>
	 * @param deadDescendants Set<N>
	 */
	private void getDeadDescendantsRecursive(SearchNode representativeNode, IndexedDirectedGraph<SearchNode> planTree, Set<SearchNode> deadDescendants) {
		for(DefaultEdge edge:planTree.outgoingEdgesOf(representativeNode)) {
			if(planTree.getEdgeTarget(edge).getStatus().equals(NodeStatus.TERMINAL)) {
				deadDescendants.add(planTree.getEdgeTarget(edge));
			}
			else {
				this.getDeadDescendantsRecursive(planTree.getEdgeTarget(edge), planTree, deadDescendants);
			}
		}
	}
}
