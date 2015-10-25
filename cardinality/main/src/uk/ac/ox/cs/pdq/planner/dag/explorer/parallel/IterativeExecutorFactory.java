package uk.ac.ox.cs.pdq.planner.dag.explorer.parallel;

import java.util.List;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.IterativeExecutorTypes;
import uk.ac.ox.cs.pdq.planner.cardinality.CardinalityEstimator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.Dominance;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

/**
 * Creates IterativeExecutor objects
 *
 * @author Efthymia Tsamoura
 */
public class IterativeExecutorFactory {

	/**
	 * 
	 * @param executorType
	 * @param parallelThreads
	 * @param chaser
	 * @param detector
	 * @param estimator
	 * @param qualityDominance
	 * @param validators
	 * @return
	 * @throws Exception
	 */
	public static IterativeExecutor createIterativeExecutor (
			IterativeExecutorTypes executorType,
			int parallelThreads,
			Chaser chaser,
			HomomorphismDetector detector,
			CardinalityEstimator estimator,
			Dominance qualityDominance,
			Dominance[] dominance,
			List<Validator> validators) throws Exception{
		switch(executorType) {
		case MULTITHREADED:
			MultiThreadedContext mtcontext = new MultiThreadedContext(parallelThreads,
					chaser,
					detector,
					estimator,
					qualityDominance,
					dominance,
					validators);
			return new MultiThreadedExecutor(mtcontext);
		default:
			throw new java.lang.IllegalArgumentException();
		}
	}
}
