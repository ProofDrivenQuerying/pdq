// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.linear.explorer;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.planner.Explorer;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleQuery;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode.NodeStatus;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.CreationMetadata;
import uk.ac.ox.cs.pdq.planner.linear.plantree.PlanTree;
import uk.ac.ox.cs.pdq.planner.plancreation.PlanCreationUtility;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleDatabaseChaseInstance;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseManager;


/**
 * Explores the linear space of configurations. 
 * The configurations that are explored are stored in a tree and each configuration maps to a plan. 
 * Exploration proceeds roughly as follows:
 * In each step of the exploration phase, select a configuration to expand.  
 * A partial proof can be expanded if it contains schema facts that are not already exposed, and their input chase constants are accessible. 
 * Create a new configuration with facts the facts of the parent configuration \sub the the newly exposed facts.
 * Saturate the new configuration using the constraints of the accessible schema. 
 * Finally, check if the newly configuration matches the accessible query and update the best configuration appropriately.    
 * 
 *
 * @author Efthymia Tsamoura
 * @author Stefano
 *
 */
public abstract class LinearExplorer extends Explorer {

	/**  The input user query *. */
	protected final ConjunctiveQuery  query;

	/**  The accessible counterpart of the user query *. */
	protected final ConjunctiveQuery  accessibleQuery;

	/**  The accessible counterpart of the input schema *. */
	protected final AccessibleSchema accessibleSchema;

	/**  Runs the chase algorithm *. */
	protected final Chaser chaser;

	/**  Detects homomorphisms during chasing*. */
	protected final DatabaseManager connection;

	/**  Estimates the cost of a plan *. */
	protected final CostEstimator costEstimator;

	protected SearchNode bestNode;

	/**  Maximum exploration depth. */
	protected final int depth;
	
	/** The best configurations list. */
	protected List<LinearChaseConfiguration> bestConfigurationsList;

	/**
	 * The tree of plans.
	 * A new node is appended to this tree at the end of each iteration.
	 */
	protected final PlanTree<SearchNode> planTree = new PlanTree<>(DefaultEdge.class);


	/**
	 * Instantiates a new linear explorer.
	 *
	 * @param eventBus the event bus
	 * @param query 		The input user query
	 * @param accessibleSchema 		The accessible counterpart of the input schema
	 * @param chaser 		Runs the chase algorithm
	 * @param costEstimator 		Estimates the cost of a plan
	 * @param depth 		Maximum exploration depth
	 * @throws PlannerException the planner exception
	 * @throws SQLException 
	 */
	public LinearExplorer(
			EventBus eventBus, 
			ConjunctiveQuery query,
			AccessibleSchema accessibleSchema, 
			Chaser chaser,
			DatabaseManager connection,
			CostEstimator costEstimator,
			int depth
			) throws PlannerException, SQLException {
		super(eventBus);
		assert (eventBus != null);
		assert (query != null);
		assert (accessibleSchema != null);
		assert (chaser != null);
		assert (connection != null);
		assert (costEstimator != null);

		this.query = query;
		this.accessibleQuery = AccessibleQuery.createAccessibleQuery(query);
		this.accessibleSchema = accessibleSchema;
		this.chaser = chaser;
		this.connection = connection;
		this.costEstimator = costEstimator;
		this.depth = depth;
		this.initialisePlanTree();
	}

	/**
	 * Initialises the plan tree.
	 *
	 * @throws PlannerException the planner exception
	 * @throws SQLException 
	 */
	private void initialisePlanTree() throws PlannerException, SQLException {
		AccessibleChaseInstance state = (uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance) 
				new AccessibleDatabaseChaseInstance(this.query, this.accessibleSchema, this.connection, true);
		this.chaser.reasonUntilTermination(state, this.accessibleSchema.getOriginalDependencies());
		this.tick = System.nanoTime();
		LinearChaseConfiguration configuration = new LinearChaseConfiguration(state);
		SearchNode root = new LinearConfigurationNode(configuration);
		
		root.getConfiguration().detectCandidates(this.accessibleSchema);
		if (!root.getConfiguration().hasCandidates()) 
			root.setStatus(NodeStatus.TERMINAL);
		this.elapsedTime += System.nanoTime() - this.tick;
		CreationMetadata metadata = new CreationMetadata(null, this.getElapsedTime());
		root.setMetadata(metadata);
		this.eventBus.post(root);
		this.planTree.addVertex(root);
	}

	/**
	 * Returns true if there does not exist at least one ongoing node (a node that has at least one unexplored candidate)
	 * or there does not exist at least one node with depth lower than the input depth-threshold.
	 *
	 * @return boolean
	 */
	@Override
	protected boolean terminates() {
		Set<SearchNode> nodes = this.planTree.vertexSet();
		if (nodes.size() == 0) 
			return false;
		if (nodes.size() > 0) {
			for (SearchNode node:nodes) {
				if (node.getStatus() == NodeStatus.ONGOING && node.getDepth() < this.depth) 
					return false;
			}
		}
		return true;
	}


	/**
	 * Choose node.
	 *
	 * @return a vertex in the plan tree that has status ONGOING and has maximal Id (this corresponds to exploring the nodes in DFS order)
	 */
	protected SearchNode chooseNode() {
		int maxId = -1;
		SearchNode selection = null;
		for (SearchNode node:this.planTree.vertexSet()) {
			if (node.getStatus().equals(NodeStatus.ONGOING) && node.getId() > maxId && node.getDepth() < this.depth) {
				selection = node;
				maxId = node.getId();
			}
		}
		return selection;
	}
	
	public PlanTree<SearchNode> getPlanTree() {
		return this.planTree;
	}

	public SearchNode getBestNode() {
		return bestNode;
	}
	
	public RelationalTerm getBestPlan() {
		if (this.bestPlan!=null)
			return PlanCreationUtility.createFinalProjection(this.query, this.bestPlan,this.connection.getSchema());
		return null;
	}

	/**
	 * Gets the configurations.
	 *
	 * @param path the path
	 * @return 		the configuration of each node in the input path.
	 * 		The nodes are indexed using their ids.
	 */
	protected List<LinearChaseConfiguration> getConfigurations(List<Integer> path) {
		Preconditions.checkArgument(path != null && !path.isEmpty());
		List<LinearChaseConfiguration> configurations = Lists.newArrayList();
		for (Integer n: path) {
			SearchNode node = this.planTree.getVertex(n);
			Preconditions.checkNotNull(node);
			configurations.add(node.getConfiguration());
		}
		return configurations;
	}
	
}
