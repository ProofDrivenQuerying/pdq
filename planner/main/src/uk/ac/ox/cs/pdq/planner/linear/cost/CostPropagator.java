package uk.ac.ox.cs.pdq.planner.linear.cost;

import java.util.List;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.plan.LinearPlan;
import uk.ac.ox.cs.pdq.planner.linear.node.PlanTree;
import uk.ac.ox.cs.pdq.planner.linear.node.SearchNode;

/**
 * 
 * Abstract path-to-success propagation. Propagation takes place either when a
 * new success node is found or when the newly created node is globally
 * equivalent to another node of the plan tree.
 * 
 * @author Efthymia Tsamoura 
 * 
 */
public abstract class CostPropagator<T extends SearchNode> {

	protected final CostEstimator<LinearPlan> costEstimator;
	
	/** The best plan found after propagation. It is null if no plan is found */
	protected LinearPlan bestPlan = null;
	
	protected List<Integer> bestPath = null;
	
	protected CostPropagator(CostEstimator<LinearPlan> estimator) {
		this.costEstimator = estimator;
	}

	public CostEstimator<LinearPlan> getCostEstimator() {
		return this.costEstimator;
	}
	
	public LinearPlan getBestPlan() {
		return this.bestPlan;
	}
	
	public List<Integer> getBestPath() {
		return this.bestPath;
	}

	/**
	 * Propagates a path-to-success (if it exists) to the root of the input plan
	 * tree.
	 * 
	 * @param snode
	 *            The node of the plan tree where propagation starts
	 * @param planTree 
	 *            The input plan tree
	 */
	public abstract void propagate(T node, PlanTree<T> planTree);
}
