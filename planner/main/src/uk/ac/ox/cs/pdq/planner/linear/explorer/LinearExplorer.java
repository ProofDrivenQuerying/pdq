package uk.ac.ox.cs.pdq.planner.linear.explorer;

import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.accessible.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.explorer.Explorer;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.NodeFactory;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.PlanTree;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode.NodeStatus;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.CreationMetadata;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleDatabaseListState;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;


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
public abstract class LinearExplorer extends Explorer<LeftDeepPlan> {

	/** The input user query **/
	protected final Query<?> query;
	
	/** The accessible counterpart of the user query **/
	protected final Query<?> accessibleQuery;

	/** The input schema **/
	protected final Schema schema;
	
	/** The accessible counterpart of the input schema **/
	protected final AccessibleSchema accessibleSchema;

	/** Runs the chase algorithm **/
	protected final Chaser chaser;

	/** Detects homomorphisms during chasing**/
	protected final HomomorphismDetector detector;

	/** Estimates the cost of a plan **/
	protected final CostEstimator<LeftDeepPlan> costEstimator;

	/** Creates new nodes */
	private final NodeFactory nodeFactory;

	/**
	 * The tree of plans.
	 * A new node is appended to this tree at the end of each iteration.
	 * The SearchNode  can be either a SimpleNode, when simple plan propagation is employed,
	 * or a BlackBoxNode, when black box plan propagation is employed
	 */
	protected final PlanTree<SearchNode> planTree = new PlanTree<>(DefaultEdge.class);

	/** Maximum exploration depth  */
	protected final int depth;
	
	protected List<LinearChaseConfiguration> bestConfigurationsList;

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
	 * 		Maximum exploration depth
	 * @throws PlannerException
	 */
	public LinearExplorer(EventBus eventBus, 
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
		super(eventBus, collectStats);
		Preconditions.checkArgument(eventBus != null);
		Preconditions.checkArgument(nodeFactory != null);
		Preconditions.checkArgument(query != null);
		Preconditions.checkArgument(accessibleQuery != null);
		Preconditions.checkArgument(schema != null);
		Preconditions.checkArgument(accessibleSchema != null);
		Preconditions.checkArgument(chaser != null);
		Preconditions.checkArgument(detector != null);
		Preconditions.checkArgument(costEstimator != null);

		this.query = query;
		this.accessibleQuery = accessibleQuery;
		this.schema = schema;
		this.accessibleSchema = accessibleSchema;
		this.chaser = chaser;
		this.detector = detector;
		this.costEstimator = costEstimator;
		this.nodeFactory = nodeFactory;
		this.depth = depth;
		this.initialise();
	}

	/**
	 * Initialises the plan tree
	 * @throws PlannerException
	 */
	private void initialise() throws PlannerException {
		AccessibleChaseState state = null;
		state = (uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState) 
				new AccessibleDatabaseListState(this.query, this.schema, (DBHomomorphismManager) this.detector);
		this.chaser.reasonUntilTermination(state, this.query, this.schema.getDependencies());

		this.tick = System.nanoTime();
		SearchNode root = this.nodeFactory.getInstance(state);
		root.getConfiguration().detectCandidates(this.accessibleSchema);
		if (!root.getConfiguration().hasCandidates()) {
			root.setStatus(NodeStatus.TERMINAL);
		}
		this.elapsedTime += System.nanoTime() - this.tick;
		CreationMetadata metadata = new CreationMetadata(null, this.getElapsedTime());
		root.setMetadata(metadata);
		this.eventBus.post(root);
		this.planTree.addVertex(root);
	}

	/**
	 * Returns true if there does not exist at least one ongoing node (a node that has at least one unexplored candidate)
	 * or there does not exist at least one node with depth lower than the input depth-threshold
	 * @return boolean
	 */
	@Override
	protected boolean terminates() {
		Set<SearchNode> nodes = this.planTree.vertexSet();
		if (nodes.size() == 0) {
			return false;
		}
		if (nodes.size() > 0) {
			for (SearchNode node:nodes) {
				if (node.getStatus() == NodeStatus.ONGOING && node.getDepth() < this.depth) {
					return false;
				}
			}
		}
		return true;
	}


	/**
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

	/**
	 * 
	 * @return
	 */
	public NodeFactory getNodeFactory() {
		return this.nodeFactory;
	}
	
	/**
	 * 
	 * @param path
	 * @return
	 * 		the configuration of each node in the input path.
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
	
	public List<LinearChaseConfiguration> getBestConfigurationsList() {
		return this.bestConfigurationsList;
	}
	
}