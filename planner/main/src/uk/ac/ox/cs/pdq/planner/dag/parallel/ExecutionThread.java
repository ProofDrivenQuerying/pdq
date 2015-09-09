package uk.ac.ox.cs.pdq.planner.dag.parallel;

import java.util.List;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.dag.ConfigurationUtility;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;

/**
 * Provides functionality to assess the potential of a configuration (whether it is possible or not to lead to the minimum-cost configuration),
 * as well as, the validity of a configuration (whether or not it satisfies given shape restrictions)
 *
 * @author Efthymia Tsamoura
 *
 * @param 
 */
public abstract class ExecutionThread {

	/**
	 *
	 * @param configuration
	 * @param bestPlan
	 * 		Best plan found so far
	 * @param successDominance
	 * 		Performs success dominance checks
	 * @return true if the input configuration is not success dominated by the best plan
	 */
	protected Boolean getPotential(DAGChaseConfiguration configuration, DAGPlan bestPlan, SuccessDominance successDominance) {
		return ConfigurationUtility.getPotential(configuration, bestPlan, successDominance);
	}

	/**
	 *
	 * @param left
	 * @param right
	 * @param bestPlan
	 * 		Best plan found so far
	 * @param costEstimator
	 * 		Estimates a plan's cost
	 * @param successDominance
	 * 		Performs success dominance checks
	 * @return true if the configuration composed from the left and right input configurations is not success dominated by the best plan
	 */
	protected Boolean getPotential(DAGChaseConfiguration left, DAGChaseConfiguration right,
			DAGPlan bestPlan, CostEstimator<DAGPlan> costEstimator, SuccessDominance successDominance) {
		return ConfigurationUtility.getPotential(left, right, bestPlan, costEstimator, successDominance);
	}

	/**
	 *
	 * @param left
	 * @param right
	 * @param validators
	 * @param depth
	 * @return
	 * 		true if the binary configuration composed from the left and right input configurations passes the validation tests,
	 * 		i.e., satisfies given shape restrictions.
	 * 		If depth > 0, then the corresponding binary configuration must be of the given depth.
	 */
	protected boolean validate(DAGChaseConfiguration left, DAGChaseConfiguration right, List<Validator> validators, int depth) {
		return ConfigurationUtility.validate(left, right, validators, depth);
	}

	/**
	 *
	 * @param left
	 * @param right
	 * @param validators
	 * @return
	 * 		true if the binary configuration composed from the left and right input configurations passes the validation tests,
	 * 		i.e., satisfies given shape restrictions.
	 */
	protected boolean validate(DAGChaseConfiguration left, DAGChaseConfiguration right, List<Validator> validators) {
		return ConfigurationUtility.validate(left, right, validators);
	}
}
