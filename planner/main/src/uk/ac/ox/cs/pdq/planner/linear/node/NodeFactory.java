package uk.ac.ox.cs.pdq.planner.linear.node;

import java.util.Random;
import java.util.Set;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.SimpleCostEstimator;
import uk.ac.ox.cs.pdq.plan.LinearPlan;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;

import com.google.common.base.Preconditions;

/**
 * Creates plan tree nodes 
 *
 * @author Efthymia Tsamoura
 */
public final class NodeFactory {
	
	private final PlannerParameters plannerParameters;
	
	/** Estimates the cost of linear plan visited during exploration.
	 * If  instance of SimpleCostEstimator, then the factory returns a simple node. Otherwise, a blackbox one. **/
	private final CostEstimator<LinearPlan> costEstimator;
	
	private final Random random;

	public NodeFactory(PlannerParameters parameters, CostEstimator<LinearPlan> costEstimator) {
		Preconditions.checkNotNull(parameters);
		Preconditions.checkNotNull(costEstimator);
		this.plannerParameters = parameters;
		this.costEstimator = costEstimator;
		this.random = new Random(this.plannerParameters.getSeed());
	}
	
	/**
	 * 
	 * @param state
	 * @return a node with the input accessible chase state
	 * @throws PlannerException
	 */
	public SearchNode getInstance(AccessibleChaseState state) throws PlannerException {
		Preconditions.checkNotNull(state);
		LinearChaseConfiguration configuration = new LinearChaseConfiguration(state, this.random);
		if (this.costEstimator instanceof SimpleCostEstimator) {
			return new SimpleNode(configuration);
		}
		return new BlackBoxNode(configuration);
	}

	/**
	 * @param parent
	 * @param exposedCandidates
	 * @return a node that exposes the input candidate facts and has as parent the input node
	 * @throws PlannerException
	 */
	public SearchNode getInstance(SearchNode parent, Set<Candidate> exposedCandidates) throws PlannerException {
		LinearChaseConfiguration configuration = new LinearChaseConfiguration(
				parent.getConfiguration(),
				exposedCandidates,
				this.random);
		if (parent instanceof SimpleNode) {
			return new SimpleNode((SimpleNode) parent, configuration);
		}
		return new BlackBoxNode((BlackBoxNode) parent, configuration);
	}
	
}