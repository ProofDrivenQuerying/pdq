package uk.ac.ox.cs.pdq.planner.linear.explorer;

import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.DOMINANCE_PRUNING;
import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.EQUIVALENCE_PRUNING;
import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.HIGHER_COST_PRUNING;
import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.MILLI_CLOSE;
import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.MILLI_DOMINANCE;
import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.MILLI_EQUIVALENCE;
import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.MILLI_QUERY_MATCH;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;

import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.algebra.AlgebraUtilities;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.linear.LinearConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.cost.CostPropagator;
import uk.ac.ox.cs.pdq.planner.linear.cost.CostPropagatorUtility;
import uk.ac.ox.cs.pdq.planner.linear.cost.SimplePropagator;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.NodeFactory;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode.NodeStatus;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.util.LimitReachedException;

// TODO: Auto-generated Javadoc
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
	 * @param eventBus the event bus
	 * @param collectStats the collect stats
	 * @param query 		The input user query
	 * @param accessibleQuery 		The accessible counterpart of the user query
	 * @param schema 		The input schema
	 * @param accessibleSchema 		The accessible counterpart of the input schema
	 * @param chaser 		Runs the chase algorithm
	 * @param detector 		Detects homomorphisms during chasing
	 * @param costEstimator 		Estimates the cost of a plan
	 * @param nodeFactory the node factory
	 * @param depth the depth
	 * @param chaseInterval the chase interval
	 * @param reasoningParameters 
	 * @throws PlannerException the planner exception
	 * @throws SQLException 
	 */
	public LinearKChase(
			EventBus eventBus, 
			boolean collectStats,
			ConjunctiveQuery query,
			ConjunctiveQuery accessibleQuery,
			AccessibleSchema accessibleSchema, 
			Chaser chaser,
			DatabaseConnection dbConn,
			CostEstimator costEstimator,
			NodeFactory nodeFactory,
			int depth,
			int chaseInterval, ReasoningParameters reasoningParameters) throws PlannerException, SQLException {
		super(eventBus, collectStats, query, accessibleQuery, accessibleSchema, chaser, dbConn, costEstimator, nodeFactory, depth, reasoningParameters);
		this.costPropagator = CostPropagatorUtility.getPropagator(costEstimator);
		this.chaseInterval = chaseInterval;
	}

	/**
	 * _explore.
	 *
	 * @throws PlannerException the planner exception
	 * @throws LimitReachedException the limit reached exception
	 */
	@Override
	protected void _explore() throws PlannerException, LimitReachedException {
		log.debug("Iteration: " + this.rounds);
		if(this.rounds % this.chaseInterval != 0 ) {
			// Choose the next node to explore below it
			SearchNode selectedNode = this.chooseNode();
			if (selectedNode == null) 
				return;
			LinearConfiguration selectedConfig = selectedNode.getConfiguration();
			/*
			 * Choose a new candidate fact. A candidate fact F(c1,c2,...,cN) is one for which
			 * (i) there exists Accessible(c_i) facts for any c_i
			 * (ii) AccessedF(c1,c2,...,cN) does not exist in the current initialConfig
			 */
			Candidate selectedCandidate = selectedConfig.chooseCandidate();
			if(selectedCandidate == null) {
				selectedNode.setStatus(NodeStatus.TERMINAL);
				return;
			}

			// Search for other candidate facts that could be exposed along with the selected candidate. 
			Set<Candidate> similarCandidates = selectedConfig.getSimilarCandidates(selectedCandidate);
			selectedConfig.removeCandidates(similarCandidates);
			if (!selectedConfig.hasCandidates()) 
				selectedNode.setStatus(NodeStatus.TERMINAL);
			
			// Create a new node from the exposed facts and add it to the plan tree
			SearchNode freshNode = this.nodeFactory.getInstance(selectedNode, similarCandidates);	
			freshNode.getConfiguration().detectCandidates(this.accessibleSchema);
			if (!freshNode.getConfiguration().hasCandidates()) 
				freshNode.setStatus(NodeStatus.TERMINAL);
		
			Cost cost = this.costEstimator.cost(freshNode.getConfiguration().getPlan());
			freshNode.getConfiguration().setCost(cost);
			
			this.planTree.addVertex(freshNode);
			this.planTree.addEdge(selectedNode, freshNode, new DefaultEdge());

			boolean domination = false;
			/* If the cost of the plan of the newly created node is higher than the best plan found so far then kill the newly created node  */
//			RelationalTerm freshNodePlan = freshNode.getConfiguration().getPlan();
			if (this.bestPlan != null) {
				if (freshNode.getConfiguration().getCost().greaterOrEquals(this.bestCost)) {
					domination = true;
					freshNode.setDominatingPlan(this.bestPlan);
					freshNode.setCostOfDominatingPlan(this.bestCost);
					this.stats.increase(HIGHER_COST_PRUNING, 1);
					log.debug(freshNode.getConfiguration().getPlan() + " has higher cost than plan " + this.bestPlan + " Costs " + freshNode.getConfiguration().getCost() + ">=" + this.bestCost);
				}
			}

			/* If at least one node in the plan tree dominates the newly created node, then kill the newly created node   */
			if (!domination && this.costPropagator instanceof SimplePropagator) {
				this.stats.start(MILLI_DOMINANCE);
				SearchNode dominatingNode = ExplorerUtility.isDominated(ExplorerUtility.getFullyGeneratedNodes(this.planTree), freshNode);
				this.stats.stop(MILLI_DOMINANCE);
				if(dominatingNode != null) {
					domination = true;
					freshNode.setDominatingPlan(dominatingNode.getConfiguration().getPlan());
					freshNode.setCostOfDominatingPlan(dominatingNode.getConfiguration().getCost());
					this.stats.increase(DOMINANCE_PRUNING, 1);
					log.debug(dominatingNode.getConfiguration().getPlan() + " dominates " + freshNode.getConfiguration().getPlan() + dominatingNode.getConfiguration().getCost() + "<" + freshNode.getConfiguration().getCost());
				}
			}
			if (domination) {
				freshNode.setStatus(NodeStatus.TERMINAL);
				this.eventBus.post(freshNode);
				this.planTree.removeVertex(freshNode);
			}
		}
		else {
			Collection<SearchNode> leaves = ExplorerUtility.getPartiallyGeneratedLeaves(this.planTree);
			log.debug("Number of partially generated leaves " + leaves.size());
			this.stats.start(MILLI_CLOSE);
			for(SearchNode leaf:leaves) {
				leaf.close(this.chaser, this.accessibleSchema.getInferredAccessibilityAxioms());
				log.debug("Close leaf: " + leaf);
			}	
			this.stats.stop(MILLI_CLOSE);

			// Perform global equivalence checks
			for(SearchNode leaf: leaves) {
				this.stats.start(MILLI_EQUIVALENCE);
				SearchNode parentEquivalent = ExplorerUtility.isEquivalent(leaves, leaf);
				this.stats.stop(MILLI_EQUIVALENCE);

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
					this.stats.increase(EQUIVALENCE_PRUNING, 1);
				}
			}

			// Check for query match
			for (SearchNode leaf: leaves) {
				if((leaf.getStatus() == NodeStatus.TERMINAL || leaf.getStatus() == NodeStatus.ONGOING) && leaf.getEquivalentNode() == null) {
					this.stats.start(MILLI_QUERY_MATCH);
					List<Match> matches = leaf.matchesQuery(this.accessibleQuery);
					this.stats.stop(MILLI_QUERY_MATCH);

					// If there exists at least one query match
					if (!matches.isEmpty()) {
						leaf.setStatus(NodeStatus.SUCCESSFUL);
						SearchNode parentNode = this.planTree.getParent(leaf);
						this.updateBestPlan(parentNode, leaf);
					}
				}
			}
		}
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
			this.bestConfigurationsList = this.getConfigurations(freshNode.getBestPathFromRoot());
			this.eventBus.post(this.getBestPlan());
			log.trace("\t+++BEST PLAN: " + AlgebraUtilities.getAccesses(this.bestPlan) + " " + this.bestCost);
		}
	}
}
