package uk.ac.ox.cs.pdq.planner.linear.explorer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.junit.Assert;

import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.planner.Explorer;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.NodeFactory;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode.NodeStatus;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleDatabaseChaseInstance;
import uk.ac.ox.cs.pdq.planner.util.PlanTree;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;


// TODO: Auto-generated Javadoc
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
	protected final DatabaseConnection connection;

	/**  Estimates the cost of a plan *. */
	protected final CostEstimator costEstimator;

	/**  Creates new nodes. */
	protected final NodeFactory nodeFactory;

	/**
	 * The tree of plans.
	 * A new node is appended to this tree at the end of each iteration.
	 * The SearchNode  can be either a SimpleNode, when simple plan propagation is employed,
	 * or a BlackBoxNode, when black box plan propagation is employed
	 */
	protected final PlanTree<SearchNode> planTree = new PlanTree<>(DefaultEdge.class);

	/**  Maximum exploration depth. */
	protected final int depth;

	/** The best configurations list. */
	protected List<LinearChaseConfiguration> bestConfigurationsList;

	private ReasoningParameters reasoningParameters;

	/**
	 * Instantiates a new linear explorer.
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
	 * @param depth 		Maximum exploration depth
	 * @throws PlannerException the planner exception
	 * @throws SQLException 
	 */
	public LinearExplorer(EventBus eventBus, 
			boolean collectStats,
			ConjunctiveQuery query,
			ConjunctiveQuery accessibleQuery,
			AccessibleSchema accessibleSchema, 
			Chaser chaser,
			DatabaseConnection dbConn,
			CostEstimator costEstimator,
			NodeFactory nodeFactory,
			int depth,
			ReasoningParameters reasoningParams
			) throws PlannerException, SQLException {
		super(eventBus, collectStats);
		Assert.assertNotNull(eventBus);
		Assert.assertNotNull(nodeFactory);
		Assert.assertNotNull(query);
		Assert.assertNotNull(accessibleQuery);
		Assert.assertNotNull(accessibleSchema);
		Assert.assertNotNull(chaser);
		Assert.assertNotNull(dbConn);
		Assert.assertNotNull(costEstimator);

		this.query = query;
		this.accessibleQuery = accessibleQuery;
		this.accessibleSchema = accessibleSchema;
		this.chaser = chaser;
		this.connection = dbConn;
		this.costEstimator = costEstimator;
		this.nodeFactory = nodeFactory;
		this.depth = depth;
		this.reasoningParameters = reasoningParams;
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
				new AccessibleDatabaseChaseInstance(this.reasoningParameters, this.query, this.accessibleSchema, this.connection, true);
		this.chaser.reasonUntilTermination(state, this.accessibleSchema.getOriginalDependencies());
		this.tick = System.nanoTime();
		SearchNode root = this.nodeFactory.getInstance(state);
		root.getConfiguration().detectCandidates(this.accessibleSchema);
		if (!root.getConfiguration().hasCandidates()) 
			root.setStatus(NodeStatus.TERMINAL);
		this.elapsedTime += System.nanoTime() - this.tick;
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
		for (SearchNode snode:this.planTree.vertexSet()) {
			if (snode.getStatus() == NodeStatus.ONGOING && snode.getId() > maxId) {
				selection = snode;
				maxId = snode.getId();
			}
		}
		return selection;
	}

//	/**
//	 * Gets the node factory.
//	 *
//	 * @return the node factory
//	 */
//	public NodeFactory getNodeFactory() {
//		return this.nodeFactory;
//	}

	/**
	 *
	 * @param path the path
	 * @return 		the configuration of each node in the input path.
	 * 		The nodes are indexed using their ids.
	 */
	protected List<LinearChaseConfiguration> getConfigurations(List<Integer> path) {
		Assert.assertTrue(path != null && !path.isEmpty());
		List<LinearChaseConfiguration> configurations = new ArrayList<>();
		for (Integer n: path) {
			SearchNode node = this.planTree.getVertex(n);
			Assert.assertNotNull(node);
			configurations.add(node.getConfiguration());
		}
		return configurations;
	}

	/**
	 * TOCOMMENT: WAHT IS IT!
	 *
	 * @return the best configurations list
	 */
	public List<LinearChaseConfiguration> getBestConfigurationsList() {
		return this.bestConfigurationsList;
	}

}
