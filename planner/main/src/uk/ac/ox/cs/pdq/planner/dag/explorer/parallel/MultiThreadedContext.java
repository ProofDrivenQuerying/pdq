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

// TODO: Auto-generated Javadoc
/**
 * Passes information to MultiThreadedExecutor objects.
 * Creates clones of reasoners, homomorphism detectors and cost estimators.
 * The clones will be subsequently assigned to different threads.
 *
 * @author Efthymia Tsamoura
 */
public class MultiThreadedContext implements Context{

	/**  Number of parallel threads. */
	private final int parallelThreads;
	
	/**  Saturate configurations using the chase algorithm*. */
	private final Chaser[] reasoners;
	
	/**  Detect homomorphisms during chasing*. */
	private final HomomorphismDetector[] detectors;
	
	/**  Estimate the cost of a plan*. */
	private final CostEstimator<DAGPlan>[] costEstimators;
	
	/**  Perform success domination checks*. */
	private final SuccessDominance[] successDominances;
	/** Checks whether the binary configuration composed from a given configuration pair satisfies given shape restrictions.**/
	private final List<Validator>[] validators;
	
	/**  Perform domination checks*. */
	private final Dominance[][] dominances;

	/**
	 * Instantiates a new multi threaded context.
	 *
	 * @param parallelThreads 		Number of parallel threads
	 * @param chaser 		Saturates configurations using the chase algorithm
	 * @param detector 		Detects homomorphisms during chasing
	 * @param costEstimator 		Estimates the cost of a plan
	 * @param successDominance 		Performs success domination checks
	 * @param dominance 		Perform domination checks
	 * @param validators 		Checks whether the binary configuration composed from a given configuration pair satisfies given shape restrictions.
	 * @throws Exception the exception
	 */
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
	 * Deep copy.
	 *
	 * @param validators the validators
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
	 * Deep copy.
	 *
	 * @param input the input
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
	 * Gets the parallel threads.
	 *
	 * @return int
	 */
	public int getParallelThreads() {
		return this.parallelThreads;
	}

	/**
	 * Gets the reasoners.
	 *
	 * @return Reasoner[]
	 */
	public Chaser[] getReasoners() {
		return this.reasoners;
	}

	/**
	 * Gets the detectors.
	 *
	 * @return HomomorphismDetector[]
	 */
	public HomomorphismDetector[] getDetectors() {
		return this.detectors;
	}

	/**
	 * Gets the cost estimators.
	 *
	 * @return CostEstimator<DAGPlan>[]
	 */
	public CostEstimator<DAGPlan>[] getCostEstimators() {
		return this.costEstimators;
	}

	/**
	 * Gets the success dominances.
	 *
	 * @return the success dominances
	 */
	public SuccessDominance[] getSuccessDominances() {
		return this.successDominances;
	}
	
	/**
	 * Gets the dominances.
	 *
	 * @return the dominances
	 */
	public Dominance[][] getDominances() {
		return this.dominances;
	}

	/**
	 * Gets the validators.
	 *
	 * @return the validators
	 */
	public List<Validator>[] getValidators() {
		return this.validators;
	}
}
