package uk.ac.ox.cs.pdq.planner.dag.explorer.parallel;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.dominance.Dominance;
import uk.ac.ox.cs.pdq.planner.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

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
	private final SuccessDominance[] successDominances;
	private final List<Validator>[] validators;
	private final Dominance[][] dominances;

	public MultiThreadedContext(int parallelThreads,
			Chaser chaser,
			HomomorphismDetector detector,
			CostEstimator<DAGPlan> costEstimator,
			SuccessDominance successDominance,
			Dominance[] dominance,
			List<Validator> validators) throws Exception {
		this.parallelThreads = parallelThreads;
		this.reasoners = new Chaser[this.parallelThreads];
		this.detectors = new HomomorphismDetector[this.parallelThreads];
		this.costEstimators = new CostEstimator[this.parallelThreads];
		this.successDominances = new SuccessDominance[this.parallelThreads];
		this.validators = new List[this.parallelThreads];
		this.dominances = new Dominance[this.parallelThreads][];
				
		for(int p = 0; p < this.parallelThreads; ++p) {
			this.reasoners[p] = (Chaser) chaser.clone();
			this.detectors[p] = detector.clone();
			this.costEstimators[p] = (CostEstimator<DAGPlan>) costEstimator.clone();
			this.successDominances[p] = successDominance.clone();
			this.validators[p] = deepCopy(validators);
			this.dominances[p] = deepCopy(dominance);
		}
	}
	
	/**
	 * @param input
	 * @return a deep copy of the input array of dominance objects
	 */
	private List<Validator> deepCopy(List<Validator> validators) {
		List<Validator> ret = new ArrayList<Validator>();
		for(Validator v:validators) {
			ret.add(v.clone());
		}
		return ret;
	}
	
	/**
	 * @param input
	 * @return a deep copy of the input array of dominance objects
	 */
	private Dominance[] deepCopy(Dominance[] input) {
		Dominance[] array = new Dominance[input.length];
		for(int i = 0; i < input.length; ++i) {
			array[i] = input[i].clone();
		}
		return array;
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

	public SuccessDominance[] getSuccessDominances() {
		return this.successDominances;
	}
	
	public Dominance[][] getDominances() {
		return this.dominances;
	}

	public List<Validator>[] getValidators() {
		return this.validators;
	}
}
