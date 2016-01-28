package uk.ac.ox.cs.pdq.planner.dag.explorer.parallel;

import java.util.List;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.IterativeExecutorTypes;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.dominance.Dominance;
import uk.ac.ox.cs.pdq.planner.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

/**
 * Creates IterativeExecutor objects.
 * An IterativeExecutor object provides methods to create binary configurations or to
 * identify the minimum-cost configuration among a given set of configurations
 *
 * @author Efthymia Tsamoura
 */
public class IterativeExecutorFactory {

	/**
	 * 
	 * @param executorType
	 * @param parallelThreads
	 * @param chaser
	 * 		Runs the chase algorithm
	 * @param detector
	 * 		Detects homomorphisms during chasing
	 * @param estimator
	 * 		Estimates the cost of a plan
	 * @param successDominance
	 * 		Removes success dominated configurations
	 * @param validators
	 * 		Validates pairs of configurations to be composed
	 * @return
	 * @throws Exception
	 */
	public static IterativeExecutor createIterativeExecutor (
			IterativeExecutorTypes executorType,
			int parallelThreads,
			Chaser chaser,
			HomomorphismDetector detector,
			CostEstimator<DAGPlan> estimator,
			SuccessDominance successDominance,
			Dominance[] dominance,
			List<Validator> validators) throws Exception{
		switch(executorType) {
		case MULTITHREADED:
			MultiThreadedContext mtcontext = new MultiThreadedContext(parallelThreads,
					chaser,
					detector,
					estimator,
					successDominance,
					dominance,
					validators);
			return new MultiThreadedExecutor(mtcontext);
		default:
			throw new java.lang.IllegalArgumentException();
		}
	}
}
