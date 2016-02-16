/*
 * 
 */
package uk.ac.ox.cs.pdq.planner.dag.explorer.parallel;

import java.util.List;

import uk.ac.ox.cs.pdq.planner.PlannerParameters.IterativeExecutorTypes;
import uk.ac.ox.cs.pdq.planner.cardinality.CardinalityEstimator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.Dominance;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

// TODO: Auto-generated Javadoc
/**
 * Creates IterativeExecutor objects.
 *
 * @author Efthymia Tsamoura
 */
public class IterativeExecutorFactory {

	/**
	 * Creates a new IterativeExecutor object.
	 *
	 * @param executorType the executor type
	 * @param parallelThreads the parallel threads
	 * @param chaser the chaser
	 * @param detector the detector
	 * @param estimator the estimator
	 * @param qualityDominance the quality dominance
	 * @param dominance the dominance
	 * @param validators the validators
	 * @return the iterative executor
	 * @throws Exception the exception
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
