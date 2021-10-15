// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.linear.cost;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.plantree.PlanTree;

import java.util.List;

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
	protected final CostEstimator costEstimator;
	
	/** The best plan found after propagation. It is null if no plan is found */
	protected RelationalTerm bestPlan = null;
	
	protected Cost bestCost = null;
	
	/** The best path. */
	protected List<Integer> bestPath = null;
	
	/**
	 * Instantiates a new cost propagator.
	 *
	 * @param estimator the estimator
	 */
	protected CostPropagator(CostEstimator estimator) {
		this.costEstimator = estimator;
	}

	/**
	 * Gets the cost estimator.
	 *
	 * @return the cost estimator
	 */
	public CostEstimator getCostEstimator() {
		return this.costEstimator;
	}
	
	/**
	 * Gets the best plan.
	 *
	 * @return the best plan
	 */
	public RelationalTerm getBestPlan() {
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
	
	public Cost getBestCost() {
		return this.bestCost;
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
