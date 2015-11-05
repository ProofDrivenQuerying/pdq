package uk.ac.ox.cs.pdq.planner.linear.explorer;

import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.MILLI_CLOSE;
import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.MILLI_QUERY_MATCH;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;

import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.linear.LinearConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.metadata.BestPlanMetadata;
import uk.ac.ox.cs.pdq.planner.linear.metadata.CreationMetadata;
import uk.ac.ox.cs.pdq.planner.linear.metadata.Metadata;
import uk.ac.ox.cs.pdq.planner.linear.node.NodeFactory;
import uk.ac.ox.cs.pdq.planner.linear.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.node.SearchNode.NodeStatus;
import uk.ac.ox.cs.pdq.reasoning.Match;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

import com.google.common.eventbus.EventBus;


/**
 * Exhaustively searches the plan space
 * For more information see
 * "Michael Benedikt, Balder ten Cate, Efthymia Tsamoura. Generating Low-cost Plans From Proofs"
 *
 * @author Efthymia Tsamoura
 */
public class LinearGeneric extends LinearExplorer {

	/** Logger. */
	private static Logger log = Logger.getLogger(LinearGeneric.class);


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
	 * @throws PlannerException
	 */
	public LinearGeneric(
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
			int depth) throws PlannerException {
		super(eventBus, collectStats, query, accessibleQuery, schema, accessibleSchema, chaser, detector, costEstimator, nodeFactory, depth);
	}

	/**
	 * @throws PlannerException
	 */
	@Override
	protected void _explore() throws PlannerException, LimitReachedException {
		SearchNode selectedNode;
		Candidate candidate;

		// Choose the next node to explore below it
		selectedNode = this.chooseNode();
		if (selectedNode == null) {
			return;
		}
		LinearConfiguration selectedConfig = selectedNode.getConfiguration();

		/*
		 * Choose a new candidate fact. A candidate fact F(c1,c2,...,cN) is one for which
		 * (i) there exists Accessible(c_i) facts for any c_i
		 * (ii) AccessedF(c_1,c_2,...,c_N) does not exist in the current configuration
		 */
		candidate = selectedConfig.chooseCandidate();

		// Search for other candidate facts that could be exposed along with the selected candidate.
		Set<Candidate> similarCandidates = selectedConfig.getSimilarCandidates(candidate);
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
		
		this.stats.start(MILLI_CLOSE);
		freshNode.close(this.chaser, this.accessibleQuery, this.accessibleSchema.getInferredAccessibilityAxioms());
		this.stats.stop(MILLI_CLOSE);
		
		Metadata metadata = new CreationMetadata(selectedNode, this.getElapsedTime());
		freshNode.setMetadata(metadata);
		this.eventBus.post(freshNode);

		this.planTree.addVertex(freshNode);
		this.planTree.addEdge(selectedNode, freshNode, new DefaultEdge());

		// Check for query match
		this.stats.start(MILLI_QUERY_MATCH);
		List<Match> matches = freshNode.matchesQuery(this.accessibleQuery);
		this.stats.stop(MILLI_QUERY_MATCH);

		// If there exists at least one query match
		if (!matches.isEmpty()) {
			freshNode.setStatus(NodeStatus.SUCCESSFUL);
			LeftDeepPlan successfulPlan = freshNode.getConfiguration().getPlan();
			
			// Update the best plan found so far
			if (this.bestPlan == null || (this.bestPlan != null && successfulPlan.getCost().lessThan(this.bestPlan.getCost()))) {
				this.bestPlan = successfulPlan;
				
				this.eventBus.post(freshNode.getConfiguration().getPlan());

				metadata = new BestPlanMetadata(selectedNode, this.bestPlan, freshNode.getBestPathFromRoot(), this.getElapsedTime());
				freshNode.setMetadata(metadata);
				this.eventBus.post(freshNode);
			}
		}
	}
}