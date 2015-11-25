package uk.ac.ox.cs.pdq.planner.linear.explorer;

import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.explorer.Explorer;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.metadata.CreationMetadata;
import uk.ac.ox.cs.pdq.planner.linear.node.NodeFactory;
import uk.ac.ox.cs.pdq.planner.linear.node.PlanTree;
import uk.ac.ox.cs.pdq.planner.linear.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.node.SearchNode.NodeStatus;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;


/**
 * A linear explorer
 *
 * @author Efthymia Tsamoura
 *
 * @param <S>
 * @param <N>
 */
public abstract class LinearExplorer extends Explorer<LeftDeepPlan> {

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


	/**
	 * 
	 * @param eventBus
	 * @param collectStats
	 * @param configuration The configuration of the root of the plan tree
	 * @param nodeFactory Creates new nodes
	 * @param depth Maximum exploration depth
	 * @throws PlannerException
	 */
	public LinearExplorer(EventBus eventBus, boolean collectStats,
			LinearChaseConfiguration configuration,
			NodeFactory nodeFactory,
			int depth) throws PlannerException {
		super(eventBus, collectStats);
		Preconditions.checkArgument(eventBus != null);
		Preconditions.checkArgument(configuration != null);
		Preconditions.checkArgument(nodeFactory != null);
		this.nodeFactory = nodeFactory;
		this.depth = depth;
		this.initialise(configuration);
	}

	/**
	 * Initialises the plan tree
	 * @param configuration The configuration of the root of the plan tree
	 * @throws PlannerException
	 */
	private void initialise(LinearChaseConfiguration configuration) throws PlannerException {
		this.tick = System.nanoTime();
		SearchNode root = this.nodeFactory.getInstance(configuration);
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
	 * @return NodeFactory<S,N>
	 */
	public NodeFactory getNodeFactory() {
		return this.nodeFactory;
	}
}