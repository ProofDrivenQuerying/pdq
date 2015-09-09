package uk.ac.ox.cs.pdq.planner.parallel;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.IterativeExecutorTypes;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;
import uk.ac.ox.cs.pdq.reasoning.ReasonerFactory;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

import com.google.common.eventbus.EventBus;

/**
 * Creates IterativeExecutor objects
 *
 * @author Efthymia Tsamoura
 */
public class IterativeExecutorFactory {
	/**
	 * @param executorType IterativeExecutorTypes
	 * @param parallelThreads int
	 * @param reasonerFactory ReasonerFactory
	 * @param detector HomomorphismDetector
	 * @param estimator CostEstimator<DAGPlan>
	 * @param successDominanceDetector SuccessDominance
	 * @param eventBus EventBus
	 * @param collectStats boolean
	 * @return IterativeExecutor
	 * @throws Exception
	 */
	public static <S extends AccessibleChaseState> IterativeExecutor createIterativeExecutor (
			IterativeExecutorTypes executorType,
			int parallelThreads,
			ReasonerFactory reasonerFactory,
			HomomorphismDetector detector,
			CostEstimator<DAGPlan> estimator,
			SuccessDominance successDominanceDetector,
			EventBus eventBus,
			boolean collectStats) throws Exception{
		switch(executorType) {
		case MULTITHREADED:
			MultiThreadedContext mtcontext = new MultiThreadedContext(parallelThreads,
					reasonerFactory,
					detector,
					estimator,
					successDominanceDetector,
					eventBus, collectStats);
			MultiThreadedExecutor executor = new MultiThreadedExecutor(mtcontext);
			return executor;
		default:
			throw new java.lang.IllegalArgumentException();
		}
	}
}
