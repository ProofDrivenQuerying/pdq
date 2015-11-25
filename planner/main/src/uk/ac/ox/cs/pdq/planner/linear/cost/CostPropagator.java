package uk.ac.ox.cs.pdq.planner.linear.cost;

import java.util.List;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.planner.linear.node.PlanTree;
import uk.ac.ox.cs.pdq.planner.linear.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.reasoning.Proof;

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

	protected final CostEstimator<LeftDeepPlan> costEstimator;

	/** The proof of the bestPlan */
	protected Proof bestProof = null;
	
	/** The best plan found after propagation. It is null if no plan is found */
	protected LeftDeepPlan bestPlan = null;
	
	protected List<Integer> bestPath = null;
	
	protected CostPropagator(CostEstimator<LeftDeepPlan> estimator) {
		this.costEstimator = estimator;
	}

	public CostEstimator<LeftDeepPlan> getCostEstimator() {
		return this.costEstimator;
	}
	
	public LeftDeepPlan getBestPlan() {
		return this.bestPlan;
	}

	public Proof getBestProof() {
		return this.bestProof;
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
