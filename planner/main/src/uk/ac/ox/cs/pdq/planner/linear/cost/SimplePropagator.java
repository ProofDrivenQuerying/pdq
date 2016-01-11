package uk.ac.ox.cs.pdq.planner.linear.cost;

import java.util.List;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;

import uk.ac.ox.cs.pdq.cost.estimators.SimpleCostEstimator;
import uk.ac.ox.cs.pdq.plan.Cost;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.PlanTree;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SimpleNode;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode.NodeStatus;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 *
 * Simple cost propagator. According to this propagation
 * paradigm, each node keeps at most one path-to-success (the one that
 * corresponds to the minimum cost sub-plan).
 *
 * The nodes of the input plan tree must be of SimpleNode type; otherwise,
 * a runtime exception is thrown. For more information about this type of
 * propagation see "Michael Benedikt, Balder ten Cate, Efthymia Tsamoura.
 * Generating Low-cost Plans From Proofs"
 *
 * @author Efthymia Tsamoura
 */
public class SimplePropagator extends CostPropagator<SimpleNode> {

	/** Logger. */
	private static Logger log = Logger.getLogger(SimplePropagator.class);
	
	/**
	 * Empty constructor
	 * @param estimator SimpleCostEstimator<LeftDeepPlan>
	 */
	public SimplePropagator(SimpleCostEstimator<LeftDeepPlan> estimator) {
		super(estimator);
	}

	/**
	 * Iterate over all children of the given node.
	 * If a child's path-to-success corresponds to a lower cost plan than
	 * the path-to-success of the input node, then the former is copied to the
	 * input node.
	 *
	 * This is applied recursive up to the root.
	 *
	 * @param node SimpleNode
	 * @param planTree PlanTree<SimpleNode>
	 * @see uk.ac.ox.cs.pdq.plan.cost.CostPropagator#propagate(uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode, org.jgrapht.DirectedGraph, uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode)
	 */
	@Override
	public void propagate(SimpleNode node, PlanTree<SimpleNode> planTree) {
		if (node.getStatus() == NodeStatus.SUCCESSFUL) {
			node.ground();
		}
		else if (node.getPointer() != null) {
			if (node.getPointer().getPathToSuccess() != null) {
				node.setPathToSuccess(node.getPointer().getPathToSuccess());
			}

		} else {
			Cost currentCost = node.getPathToSuccess() == null ? null: 
			PropagatorUtils.createLeftDeepPlan(planTree, node.getPathToSuccess(), this.costEstimator).getCost();
			// Iterate over all children of the given node.
			for (DefaultEdge edge:planTree.outgoingEdgesOf(node)) {
				SimpleNode child = planTree.getEdgeTarget(edge);
				if (child.getPathToSuccess() != null) {
					List<Integer> sequence = Lists.newArrayList(child.getId());
					sequence.addAll(child.getPathToSuccess());
					Cost childCost = PropagatorUtils.createLeftDeepPlan(planTree, sequence, this.costEstimator).getCost();
					if (currentCost == null || childCost.lessThan(currentCost)) {
						node.setPathToSuccess(sequence);
						currentCost = PropagatorUtils.createLeftDeepPlan(planTree, node.getPathToSuccess(), this.costEstimator).getCost();
					}
				}
			}
		}


		// Update the best plan at the root if necessary
		if (node.equals(planTree.getRoot()) && node.getPathToSuccess() != null) {
			this.bestPlan = PropagatorUtils.createLeftDeepPlan(planTree, node.getPathToSuccess(), this.costEstimator);
			this.bestPath = node.getPathToSuccess();
			Preconditions.checkState(this.bestPlan != null);
			Preconditions.checkState(this.bestPath != null);
		} else {
			for (DefaultEdge edge: planTree.incomingEdgesOf(node)) {
				this.propagate(planTree.getEdgeSource(edge), planTree);
			}
			for (SimpleNode n: planTree.vertexSet()) {
				if (n.getPointer() != null && n.getPointer().equals(node)) {
					this.propagate(n, planTree);
				}
			}
		}
	}
}