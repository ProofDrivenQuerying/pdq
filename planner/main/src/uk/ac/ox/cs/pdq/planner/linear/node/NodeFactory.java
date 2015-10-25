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
 * Creates nodes based on the input arguments
 *
 * @author Efthymia Tsamoura
 */
public final class NodeFactory {
	
	private final PlannerParameters plannerParameters;
	
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
	 * @param configuration LinearConfiguration
	 * @return a node with the input configuration * @throws PlannerException
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
	 * @param parentNode
	 * @param exposedCandidates
	 * @return a node having the given parent node and the given exposed candidate facts
	 * @throws PlannerException
	 */
	public SearchNode getInstance(SearchNode parentNode, Set<Candidate> exposedCandidates) throws PlannerException {
		LinearChaseConfiguration configuration = new LinearChaseConfiguration(
				parentNode.getConfiguration(),
				exposedCandidates,
				this.random);
		if (parentNode instanceof SimpleNode) {
			return new SimpleNode((SimpleNode) parentNode, configuration);
		}
		return new BlackBoxNode((BlackBoxNode) parentNode, configuration);
	}
	
}