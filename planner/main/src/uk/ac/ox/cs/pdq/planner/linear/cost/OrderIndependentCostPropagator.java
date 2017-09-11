package uk.ac.ox.cs.pdq.planner.linear.cost;

import java.util.List;

import org.jgrapht.graph.DefaultEdge;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.estimators.OrderIndependentCostEstimator;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode.NodeStatus;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SimpleNode;
import uk.ac.ox.cs.pdq.planner.util.PlanTree;

// TODO: Auto-generated Javadoc
/**
 *
 * Simple cost propagator. This propagates information through the search space when a node has been updated; information
 * concerning the plans, successful plans, best plan etc. According to this propagation paradigm, 
 * each node keeps at most one path-to-success (the one that corresponds to the minimum cost sub-plan).
 * The nodes of the input plan tree must be of SimpleNode type; otherwise,
 * a runtime exception is thrown. For more information about this type of
 * propagation see "Michael Benedikt, Balder ten Cate, Efthymia Tsamoura.
 * Generating Low-cost Plans From Proofs"
 *
 * @author Efthymia Tsamoura
 */
public class OrderIndependentCostPropagator extends CostPropagator<SimpleNode> {

	/**
	 * Empty constructor.
	 *
	 * @param estimator SimpleCostEstimator<LeftDeepPlan>
	 */
	public OrderIndependentCostPropagator(OrderIndependentCostEstimator estimator) {
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
		if (node.getStatus() == NodeStatus.SUCCESSFUL) 
			node.ground();
		
		else if (node.getEquivalentNode() != null) {
			if (node.getEquivalentNode().getPathToSuccess() != null) 
				node.setPathToSuccess(node.getEquivalentNode().getPathToSuccess());
		} 
		else {
			RelationalTerm plan = null;
			Cost currentCost = null;
			if(node.getPathToSuccess() != null) {
				plan = CostPropagatorUtility.createLeftDeepPlan(planTree, node.getPathToSuccess());
				currentCost = this.costEstimator.cost(plan);
			}
			// Iterate over all children of the given node.
			for (DefaultEdge edge:planTree.outgoingEdgesOf(node)) {
				SimpleNode child = planTree.getEdgeTarget(edge);
				if (child.getPathToSuccess() != null) {
					List<Integer> sequence = Lists.newArrayList(child.getId());
					sequence.addAll(child.getPathToSuccess());
					plan = CostPropagatorUtility.createLeftDeepPlan(planTree, sequence);
					Cost childCost = this.costEstimator.cost(plan);
					if (currentCost == null || childCost.lessThan(currentCost)) {
						node.setPathToSuccess(sequence);
						currentCost = childCost;
					}
				}
			}
		}

		// Update the best plan at the root if necessary
		if (node.equals(planTree.getRoot()) && node.getPathToSuccess() != null) {
			this.bestPlan = CostPropagatorUtility.createLeftDeepPlan(planTree, node.getPathToSuccess());
			this.bestCost = this.costEstimator.cost(bestPlan);
			this.bestPath = node.getPathToSuccess();
			Preconditions.checkState(this.bestPlan != null);
			Preconditions.checkState(this.bestPath != null);
		} else {
			for (DefaultEdge edge: planTree.incomingEdgesOf(node)) 
				this.propagate(planTree.getEdgeSource(edge), planTree);
			
			for (SimpleNode n: planTree.vertexSet()) {
				if (n.getEquivalentNode() != null && n.getEquivalentNode().equals(node)) 
					this.propagate(n, planTree);
			}
		}
	}
}
