package uk.ac.ox.cs.pdq.planner.linear.cost;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.estimators.OrderDependentCostEstimator;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearConfigurationNode;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode.NodeStatus;
import uk.ac.ox.cs.pdq.planner.linear.plantree.PlanTree;
import uk.ac.ox.cs.pdq.planner.plancreation.PlanCreationUtility;

/**
 *
 * Black box cost propagator. When a node has been updated, this class propogates information about search nodes in the search space, their plans and associated costs.
 * According to this propagation paradigm, each node keeps all the paths-to-success. 
 * The nodes of the input plan tree must be of OrderDependent type; otherwise, a runtime exception is thrown.
 *  
 *
 *
 * @author Efthymia Tsamoura
 *
 */
public class OrderDependentCostPropagator extends CostPropagator<LinearConfigurationNode> {

	/**  the nodes that have been already updated */
	private Set<LinearConfigurationNode> updatedNodes = Sets.newHashSet();

	/**
	 * Empty constructor.
	 */
	public OrderDependentCostPropagator(OrderDependentCostEstimator estimator) {
		super(estimator);
	}


	/**
	 * Iterate over all children and copy their corresponding paths-to-success,
	 * then continue upwards, up to the plan tree root.
	 * 
	 * At the root, it finds the paths-to-success with the minimal cost, and stores
	 * it as the best plan.
	 *
	 * @param node the node
	 * @param planTree the plan tree
	 */
	@Override
	public void propagate(LinearConfigurationNode node, PlanTree<LinearConfigurationNode> planTree) {
		this.updatedNodes.clear();
		this._propagate(node, planTree);
	}

	/**
	 * _propagate.
	 *
	 * @param node LinearConfigurationNode
	 * @param planTree PlanTree<LinearConfigurationNode>
	 */
	public void _propagate(LinearConfigurationNode node, PlanTree<LinearConfigurationNode> planTree) {
		this.updatedNodes.add(node);
		if (node.getStatus() == NodeStatus.SUCCESSFUL) 
			node.ground();
		else if (node.getEquivalentNode() != null) {
			LinearConfigurationNode pointer = node.getEquivalentNode();
			if (pointer.getPathToSuccess() != null) 
				node.setPathToSuccess(pointer.getPathToSuccess());
		} else {
			Set<LinearConfigurationNode> children = new LinkedHashSet<>();
			for (DefaultEdge edge : planTree.outgoingEdgesOf(node)) 
				children.add(planTree.getEdgeTarget(edge));
			
			// Iterate over all children and copy their corresponding paths-to-success
			for (LinearConfigurationNode child:children) {
				List<Integer> path = child.getPathToSuccess();
				if (path != null) {
					ArrayList<Integer> sequence = Lists.newArrayList();
					sequence.add(child.getId());
					sequence.addAll(path);
					node.setPathToSuccess(sequence);
				}
			}
		}
		// When reaching the root, we find out which one of these 
		// paths-to-success correspond to the minimum cost plan and we return it
		if (node.equals(planTree.getRoot())) {
			List<Integer> path = node.getPathToSuccess();
			if (path != null) {
				RelationalTerm plan = PlanCreationUtility.createLeftDeepPlan(planTree, path);
				Cost cost = this.costEstimator.cost(plan);
				Preconditions.checkState(plan != null);
				if (this.bestPlan == null || cost.lessThan(this.bestCost)) {
					this.bestPlan = plan;
					this.bestCost = cost;
					this.bestPath = path;
				}
			}
		} 

		else {
			for (DefaultEdge edge: planTree.incomingEdgesOf(node)) 
				this._propagate(planTree.getEdgeSource(edge), planTree);
			
			for (LinearConfigurationNode n:planTree.vertexSet()) {
				if (n.getEquivalentNode() != null && n.getEquivalentNode().equals(node) && !this.updatedNodes.contains(n)) 
					this._propagate(n, planTree);
			}
		}
	}

}
