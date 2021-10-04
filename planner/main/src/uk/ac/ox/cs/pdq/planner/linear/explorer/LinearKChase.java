// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.linear.explorer;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.algebra.Plan;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.OrderIndependentCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.TextBookCostEstimator;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.exceptions.LimitReachedException;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.LinearConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.cost.CostPropagator;
import uk.ac.ox.cs.pdq.planner.linear.cost.OrderDependentCostPropagator;
import uk.ac.ox.cs.pdq.planner.linear.cost.OrderIndependentCostPropagator;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode.NodeStatus;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.CreationMetadata;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.DominanceMetadata;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.Metadata;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseManager;

/**
 * Searches the proof space employing several optimisations (similar to the OptimizedExplorer) in order to reach faster the best proof.
 * Performs chasing at intervals
 *
 * @author Efthymia Tsamoura
 */
public class LinearKChase extends LinearExplorer {

	private static Logger log = Logger.getLogger(LinearKChase.class);

	/**  Propagates to the root of the plan tree the best plan found so far. */
	@SuppressWarnings("rawtypes")
	protected final CostPropagator costPropagator;

	/**  How often to perform chasing. */
	private final Integer chaseInterval;

	/**
	 * Instantiates a new linear k chase.
	 *
	 * @param eventBus the event buss
	 * @param query 		The input user query
	 * @param accessibleSchema 		The accessible counterpart of the input schema
	 * @param chaser 		Runs the chase algorithm
	 * @param costEstimator 		Estimates the cost of a plan
	 * @param depth the depth
	 * @param chaseInterval the chase interval
	 * @throws PlannerException the planner exception
	 * @throws SQLException 
	 */
	@SuppressWarnings("rawtypes")
	public LinearKChase(
			EventBus eventBus, 
			ConjunctiveQuery query,
			AccessibleSchema accessibleSchema, 
			Chaser chaser,
			DatabaseManager connection,
			CostEstimator costEstimator,
			CostPropagator costPropagator,
			int depth,
			int chaseInterval) throws PlannerException, SQLException {
		super(eventBus, query, accessibleSchema, chaser, connection, costEstimator, depth);
		Preconditions.checkNotNull(costPropagator);
		Preconditions.checkArgument(costPropagator instanceof OrderIndependentCostPropagator && costEstimator instanceof OrderIndependentCostEstimator
				|| costPropagator instanceof OrderDependentCostPropagator && costEstimator instanceof TextBookCostEstimator);
		this.costPropagator = costPropagator;
		this.chaseInterval = chaseInterval;
	}

	/**
	 * It will explore without chasing K times and then does a chase step. Exceptional cases:
	 *   - when K=1 it will do both parts in each round.
	 *   - before termination it will do a chasing round. 
	 *
	 * @throws PlannerException the planner exception
	 * @throws LimitReachedException the limit reached exception
	 */
	@Override
	public void performSingleExplorationStep() throws PlannerException, LimitReachedException {
		_performSingleExplorationStep();
	}
	
	public SearchNode _performSingleExplorationStep() throws PlannerException, LimitReachedException {
		log.debug("Iteration: " + this.rounds);
		SearchNode freshNode = null;
		if(this.rounds % this.chaseInterval != 0  || chaseInterval==1) {
			freshNode = _performSingleExplorationStepWithoutChasing();
		}
		if (this.terminates() || chaseInterval==1 || this.rounds % this.chaseInterval == 0 ) {
			// we do cases when:
			//  - This is the last round (exploration will terminate)
			//  - chaseInterval == 1, so we do chase in every round,
			//  - this is the K round without chasing (this.rounds % this.chaseInterval == 0)
			_performSingleExplorationStepWithChasing();
		}
		this.rounds++;
		return freshNode;
	}
	
	/** 
	 * Does the exploration step in case when the round_number is a multiple of K. chasing is needed. 
	 * 
	 * @throws PlannerException
	 * @throws LimitReachedException
	 */
	private void _performSingleExplorationStepWithChasing() throws PlannerException, LimitReachedException {
		Collection<SearchNode> leaves = getLeafNodesThatAreNotFullyChased(this.planTree);
		log.debug("Number of partially generated leaves " + leaves.size());
		for(SearchNode leaf:leaves) {
			leaf.close(this.chaser, this.accessibleSchema.getInferredAccessibilityAxioms());
			log.debug("Close leaf: " + leaf);
		}	

		// Perform global equivalence checks
		for(SearchNode leaf: leaves) {
			SearchNode parentEquivalent = SearchNode.isEquivalent(leaves, leaf);

			/*
			 * If such a node exists then
			 * -create a pointer from the newly created node to the one that is globally equivalent to
			 * -propagate upwards the best paths to success
			 * -update the best plan found so far
			 * -stop exploring plans below the newly created node
			 */
			if (parentEquivalent != null && !parentEquivalent.getStatus().equals(NodeStatus.SUCCESSFUL)) {
				leaf.setEquivalentNode(parentEquivalent);
				leaf.setStatus(NodeStatus.TERMINAL);
				SearchNode parentNode = this.planTree.getParent(leaf);
				this.updateBestPlan(parentNode, leaf);
				log.debug("Node " + parentEquivalent.toString() + " is equivalent to " + leaf.toString());
			}
		}

		// Check for query match
		for (SearchNode leaf:leaves) {
			if((leaf.getStatus() == NodeStatus.TERMINAL || leaf.getStatus() == NodeStatus.ONGOING) && leaf.getEquivalentNode() == null) {
				List<Match> matches = leaf.matchesQuery(this.accessibleQuery);

				// If there exists at least one query match
				if (!matches.isEmpty()) {
					leaf.setStatus(NodeStatus.SUCCESSFUL);
					SearchNode parentNode = this.planTree.getParent(leaf);
					this.updateBestPlan(parentNode, leaf);
				}
			}
		}
	}

	/** 
	 * Does the exploration step in case when the round_number is not multiple of K. No chasing this time.    
	 * 
	 * @throws PlannerException
	 * @throws LimitReachedException
	 */
	private SearchNode _performSingleExplorationStepWithoutChasing() throws PlannerException {
		// Choose the next node to explore below it
		SearchNode selectedNode = this.chooseNode();
		if (selectedNode == null) 
			return null;
		LinearConfiguration selectedConfig = selectedNode.getConfiguration();
		/*
		 * Choose a new candidate fact. A candidate fact F(c1,c2,...,cN) is one for which
		 * (i) there exists Accessible(c_i) facts for any c_i
		 * (ii) AccessedF(c1,c2,...,cN) does not exist in the current initialConfig
		 */
		Candidate selectedCandidate = selectedConfig.chooseCandidate();
		if(selectedCandidate == null) {
			selectedNode.setStatus(NodeStatus.TERMINAL);
			Metadata metadata = new Metadata(selectedNode, this.getElapsedTime());
			selectedNode.setMetadata(metadata);
			this.eventBus.post(selectedNode);
			return null;
		}

		// Search for other candidate facts that could be exposed along with the selected candidate. 
		Set<Candidate> similarCandidates = selectedConfig.getSimilarCandidates(selectedCandidate);
		selectedConfig.removeCandidates(similarCandidates);
		if (!selectedConfig.hasCandidates()) 
			selectedNode.setStatus(NodeStatus.TERMINAL);
		
		// Create a new node from the exposed facts and add it to the plan tree
		LinearChaseConfiguration newConfiguration = new LinearChaseConfiguration(
				selectedNode.getConfiguration(),
				similarCandidates);
		SearchNode freshNode = new LinearConfigurationNode((LinearConfigurationNode) selectedNode, newConfiguration);
		
		freshNode.getConfiguration().detectCandidates(this.accessibleSchema);
		if (!freshNode.getConfiguration().hasCandidates()) 
			freshNode.setStatus(NodeStatus.TERMINAL);
	
		Cost cost = this.costEstimator.cost(freshNode.getConfiguration().getPlan());
		freshNode.getConfiguration().setCost(cost);
		freshNode.setCostOfBestPlanFromRoot(cost);
		
		Metadata metadata = new CreationMetadata(selectedNode, this.getElapsedTime());
		freshNode.setMetadata(metadata);
		this.eventBus.post(freshNode);
		
		this.planTree.addVertex(freshNode);
		this.planTree.addEdge(selectedNode, freshNode, new DefaultEdge());

		boolean domination = false;
		/* If the cost of the plan of the newly created node is higher than the best plan found so far then kill the newly created node  */
		Plan freshNodePlan = freshNode.getConfiguration().getPlan();
		if (this.bestPlan != null) {
			if (freshNode.getConfiguration().getCost().greaterOrEquals(this.bestCost)) {
				domination = true;
				freshNode.setDominatingPlan(this.bestPlan);
				freshNode.setCostOfDominatingPlan(this.bestCost);
				metadata = new DominanceMetadata(selectedNode, this.bestPlan, freshNodePlan, this.getElapsedTime());
				freshNode.setMetadata(metadata);
				log.debug(freshNode.getConfiguration().getPlan() + " has higher cost than plan " + this.bestPlan + " Costs " + freshNode.getConfiguration().getCost() + ">=" + this.bestCost);
			}
		}

		/* If at least one node in the plan tree dominates the newly created node, then kill the newly created node   */
		if (!domination && this.costPropagator instanceof OrderIndependentCostPropagator) {
			SearchNode dominatingNode = SearchNode.isCostAndFactDominated(getNodesThatAreFullyChased(this.planTree), freshNode);
			if(dominatingNode != null) {
				domination = true;
				Plan dominancePlan = dominatingNode.getConfiguration().getPlan();
				freshNode.setDominatingPlan(dominatingNode.getConfiguration().getPlan());
				freshNode.setCostOfDominatingPlan(dominatingNode.getConfiguration().getCost());
				metadata = new DominanceMetadata(dominatingNode, dominancePlan, freshNodePlan, this.getElapsedTime());
				freshNode.setMetadata(metadata);
				log.debug(dominatingNode.getConfiguration().getPlan() + " dominates " + freshNode.getConfiguration().getPlan() + dominatingNode.getConfiguration().getCost() + "<" + freshNode.getConfiguration().getCost());
			}
		}
		if (domination) {
			freshNode.setStatus(NodeStatus.TERMINAL);
			this.eventBus.post(freshNode);
			this.planTree.removeVertex(freshNode);
		}
		return freshNode;
	}

	/**
	 * Update best plan.
	 *
	 * @param parentNode the parent node
	 * @param freshNode the fresh node
	 */
	@SuppressWarnings("unchecked")
	private void updateBestPlan(SearchNode parentNode, SearchNode freshNode) {
		this.costPropagator.propagate(freshNode, this.planTree);
		RelationalTerm successfulPlan = this.costPropagator.getBestPlan();
		Cost costOfSuccessfulPlan = this.costPropagator.getBestCost();
		if (this.bestPlan == null && successfulPlan != null || 
			this.bestPlan != null && successfulPlan != null && costOfSuccessfulPlan.lessThan(this.bestCost)) {
			this.bestPlan = successfulPlan;
			this.bestCost = costOfSuccessfulPlan;
			this.eventBus.post(this.getBestPlan());
			log.trace("\t+++BEST PLAN: " + this.bestPlan.getAccesses() + " " + this.bestCost);
			this.eventBus.post(freshNode);

		}
	}
	

	/**
	 * Searches through the planTree to find candidate nodes that haven't been fully chased. 
	 *
	 * @param <N> the number type
	 * @param planTree the plan tree
	 * @return the partially generated leaves
	 */
	private static <N extends SearchNode> Collection<N> getLeafNodesThatAreNotFullyChased(DirectedGraph<N, DefaultEdge> planTree) {
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
	private static <N extends SearchNode> Collection<N> getNodesThatAreFullyChased(DirectedGraph<N, DefaultEdge> planTree) {
		Collection<N> fullyGenerated = new LinkedHashSet<>();
		for (N node: planTree.vertexSet()) {
			if (node.isFullyChased()) 
				fullyGenerated.add(node);
		}
		return fullyGenerated;
	}
	
}
