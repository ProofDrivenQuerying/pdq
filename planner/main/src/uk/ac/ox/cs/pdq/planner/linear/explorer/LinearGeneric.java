package uk.ac.ox.cs.pdq.planner.linear.explorer;

import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.MILLI_CLOSE;
import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.MILLI_QUERY_MATCH;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;

import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.LinearConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.metadata.BestPlanMetadata;
import uk.ac.ox.cs.pdq.planner.linear.metadata.CreationMetadata;
import uk.ac.ox.cs.pdq.planner.linear.metadata.Metadata;
import uk.ac.ox.cs.pdq.planner.linear.node.NodeFactory;
import uk.ac.ox.cs.pdq.planner.linear.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.node.SearchNode.NodeStatus;
import uk.ac.ox.cs.pdq.reasoning.Match;

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
	 * @param costEstimator Estimates the cost of the plans found
	 * @param configuration The configuration of the root of the plan tree
	 * @param nodeFactory Creates new nodes
	 * @param depth Maximum exploration depth
	 * @throws PlannerException
	 */
	public LinearGeneric(
			EventBus eventBus, boolean collectStats,
			CostEstimator<LeftDeepPlan> costEstimator,
			LinearChaseConfiguration configuration,
			NodeFactory nodeFactory,
			int depth) throws PlannerException {
		super(eventBus, collectStats, 
				configuration, nodeFactory, depth);
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
		this.stats.start(MILLI_CLOSE);
		freshNode.close();
		this.stats.stop(MILLI_CLOSE);
		
		Metadata metadata = new CreationMetadata(selectedNode, this.getElapsedTime());
		freshNode.setMetadata(metadata);
		this.eventBus.post(freshNode);


		this.planTree.addVertex(freshNode);
		this.planTree.addEdge(selectedNode, freshNode, new DefaultEdge());


		// Check for query match
		this.stats.start(MILLI_QUERY_MATCH);
		List<Match> matches = freshNode.matchesQuery();//this.getAccessibleQuery(), this.getQuery().getFreeToCanonical()
		this.stats.stop(MILLI_QUERY_MATCH);

		// If there exists at least one query match
		if (!matches.isEmpty()) {
			freshNode.setStatus(NodeStatus.SUCCESSFUL);
			LeftDeepPlan successfulPlan = freshNode.getConfiguration().getPlan();
			
			// Update the best plan found so far
			if (this.bestPlan == null || (this.bestPlan != null && successfulPlan.getCost().lessThan(this.bestPlan.getCost()))) {
				this.bestPlan = successfulPlan;
				
				this.eventBus.post(freshNode.getConfiguration().getPlan());
				this.bestProof = freshNode.getConfiguration().getProof();
				this.eventBus.post(this.bestProof);
				
				metadata = new BestPlanMetadata(selectedNode, this.bestPlan, this.bestProof, freshNode.getBestPathFromRoot(), this.getElapsedTime());
				freshNode.setMetadata(metadata);
				this.eventBus.post(freshNode);
			}
		}
	}
}