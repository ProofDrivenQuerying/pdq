package uk.ac.ox.cs.pdq.planner.linear.cost;

import java.util.List;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.PlanTree;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;

// TODO: Auto-generated Javadoc
/**
 * Abstract path-to-success propagation. Propagation takes place either when a
 * new success node is found or when the newly created node is globally
 * equivalent to another node of the plan tree.
 *
 * @author Efthymia Tsamoura
 * @param <T> the generic type
 */
public abstract class CostPropagator<T extends SearchNode> {

	/** The cost estimator. */
	protected final CostEstimator<LeftDeepPlan> costEstimator;
	
	/** The best plan found after propagation. It is null if no plan is found */
	protected LeftDeepPlan bestPlan = null;
	
	/** The best path. */
	protected List<Integer> bestPath = null;
	
	/**
	 * Instantiates a new cost propagator.
	 *
	 * @param estimator the estimator
	 */
	protected CostPropagator(CostEstimator<LeftDeepPlan> estimator) {
		this.costEstimator = estimator;
	}

	/**
	 * Gets the cost estimator.
	 *
	 * @return the cost estimator
	 */
	public CostEstimator<LeftDeepPlan> getCostEstimator() {
		return this.costEstimator;
	}
	
	/**
	 * Gets the best plan.
	 *
	 * @return the best plan
	 */
	public LeftDeepPlan getBestPlan() {
		return this.bestPlan;
	}
	
	/**
	 * Gets the best path.
	 *
	 * @return the best path
	 */
	public List<Integer> getBestPath() {
		return this.bestPath;
	} 

	/**
	 * Propagates a path-to-success (if it exists) to the root of the input plan
	 * tree.
	 *
	 * @param node the node
	 * @param planTree            The input plan tree
	 */
	public abstract void propagate(T node, PlanTree<T> planTree);
}
