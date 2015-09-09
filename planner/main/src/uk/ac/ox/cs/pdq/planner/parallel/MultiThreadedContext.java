package uk.ac.ox.cs.pdq.planner.parallel;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.reasoning.ReasonerFactory;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

import com.google.common.eventbus.EventBus;

/**
 * Passes information to MultiThreadedExecutor objects.
 * Creates clones of reasoners, homomorphism detectors and cost estimators.
 * The clones will be subsequently assigned to different threads.
 *
 * @author Efthymia Tsamoura
 */
public class MultiThreadedContext implements Context{

	/** Number of parallel threads*/
	private final int parallelThreads;
	private final Chaser[] reasoners;
	private final HomomorphismDetector[] detectors;
	private final CostEstimator<DAGPlan>[] costEstimators;

	/**
	 * Constructor for MultiThreadedContext.
	 * @param parallelThreads int
	 * @param reasonerFactory ReasonerFactory
	 * @param detector HomomorphismDetector
	 * @param costEstimator CostEstimator<DAGPlan>
	 * @param successDominanceDetector SuccessDominance
	 * @param eventBus EventBus
	 * @param collectStats boolean
	 * @throws Exception
	 */
	public MultiThreadedContext(int parallelThreads,
			ReasonerFactory reasonerFactory,
			HomomorphismDetector detector,
			CostEstimator<DAGPlan> costEstimator,
			SuccessDominance successDominanceDetector,
			EventBus eventBus,
			boolean collectStats) throws Exception {
		this.parallelThreads = parallelThreads;
		this.reasoners = new Chaser[this.parallelThreads];
		this.detectors = new HomomorphismDetector[this.parallelThreads];
		this.costEstimators = new CostEstimator[this.parallelThreads];
		for(int p = 0; p < this.parallelThreads; ++p) {
			this.reasoners[p] = reasonerFactory.getInstance();
			this.detectors[p] = detector.clone();
			this.costEstimators[p] = (CostEstimator<DAGPlan>) costEstimator.clone();
		}
	}

	/**
	 * @return int
	 */
	public int getParallelThreads() {
		return this.parallelThreads;
	}

	/**
	 * @return Reasoner[]
	 */
	public Chaser[] getReasoners() {
		return this.reasoners;
	}

	/**
	 * @return HomomorphismDetector[]
	 */
	public HomomorphismDetector[] getDetectors() {
		return this.detectors;
	}

	/**
	 * @return CostEstimator<DAGPlan>[]
	 */
	public CostEstimator<DAGPlan>[] getCostEstimators() {
		return this.costEstimators;
	}
}
