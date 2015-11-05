package uk.ac.ox.cs.pdq.planner.linear.cost;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

import uk.ac.ox.cs.pdq.cost.estimators.BlackBoxCostEstimator;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.planner.linear.node.BlackBoxNode;
import uk.ac.ox.cs.pdq.planner.linear.node.PlanTree;
import uk.ac.ox.cs.pdq.planner.linear.node.SearchNode.NodeStatus;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 *
 * Black box cost propagator. According to this propagation
 * paradigm, each node keeps all the paths-to-success. The nodes of the input plan
 * tree must be of BlackBoxNode type; otherwise, a runtime exception is thrown.
 * For more information about this type of propagation see
 * "Michael Benedikt, Balder ten Cate, Efthymia Tsamoura. Generating Low-cost Plans From Proofs"
 *
 *
 * @author Efthymia Tsamoura
 *
 */
public class BlackBoxPropagator extends CostPropagator<BlackBoxNode> {

	private Set<BlackBoxNode> updated = Sets.newHashSet();

	/**
	 * Empty constructor
	 * @param estimator BlackBoxCostEstimator<LinearPlan,?>
	 */
	public BlackBoxPropagator(BlackBoxCostEstimator<LeftDeepPlan> estimator) {
		super(estimator);
	}


	/**
	 * Iterate over all children and copy their corresponding paths-to-success,
	 * then continue upwards, up to the plan tree root.
	 *
	 * At the root, it finds the paths-to-success with the minimal cost, and stores
	 * it as the best plan.
	 *
	 * @param node
	 * @param planTree
	 */
	@Override
	public void propagate(BlackBoxNode node, PlanTree<BlackBoxNode> planTree) {
		this.updated.clear();
		this._propagate(node, planTree);
	}

	/**
	 * @param node BlackBoxNode
	 * @param planTree PlanTree<BlackBoxNode>
	 */
	public void _propagate(BlackBoxNode node, PlanTree<BlackBoxNode> planTree) {
		this.updated.add(node);

		if (node.getStatus() == NodeStatus.SUCCESSFUL) {
			node.ground();
		}

		else if (node.getPointer() != null) {
			BlackBoxNode pointer = node.getPointer();
			if (pointer.getPathsToSuccess() != null) {
				node.setPathsToSuccess(pointer.getPathsToSuccess());
			}

		} else {
			Set<BlackBoxNode> children = new LinkedHashSet<>();
			for (DefaultEdge edge : planTree.outgoingEdgesOf(node)) {
				children.add(planTree.getEdgeTarget(edge));
			}

			// Iterate over all children and copy their corresponding paths-to-success
			for (BlackBoxNode child:children) {
				Set<List<Integer>> paths = child.getPathsToSuccess();
				if (paths != null) {
					for (List<Integer> path:paths) {
						ArrayList<Integer> sequence = Lists.newArrayList();
						sequence.add(child.getId());
						sequence.addAll(path);
						node.addPathToSuccess(sequence);
					}
				}
			}
		}


		// When reaching the root, we find out which one of these 
		// paths-to-success correspond to the minimum cost plan and we return it
		if (node.equals(planTree.getRoot())) {
			Set<List<Integer>> paths = node.getPathsToSuccess();
			if (paths != null) {
				for (List<Integer> path:paths) {
					LeftDeepPlan plan = PropagatorUtils.createLinearPlan(planTree, path, this.costEstimator);
					Preconditions.checkState(plan != null);
					if (this.bestPlan == null || plan.getCost().lessThan(this.bestPlan.getCost())) {
						this.bestPlan = plan;
						this.bestPath = path;
					}
				}
			}
		}

		else {
			for (DefaultEdge edge: planTree.incomingEdgesOf(node)) {
				this._propagate(planTree.getEdgeSource(edge), planTree);
			}
			for (BlackBoxNode n:planTree.vertexSet()) {
				if (n.getPointer() != null && n.getPointer().equals(node) && !this.updated.contains(n)) {
					this._propagate(n, planTree);
				}
			}
		}
	}

}
