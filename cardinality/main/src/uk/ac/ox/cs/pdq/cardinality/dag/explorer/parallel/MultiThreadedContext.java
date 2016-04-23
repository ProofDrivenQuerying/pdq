/*
 * 
 */
package uk.ac.ox.cs.pdq.cardinality.dag.explorer.parallel;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.cardinality.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.cardinality.estimator.CardinalityEstimator;
import uk.ac.ox.cs.pdq.cardinality.reasoning.chase.dominance.Dominance;
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
	
	/** The reasoners. */
	private final Chaser[] reasoners;
	
	/** The detectors. */
	private final HomomorphismDetector[] detectors;
	
	/** The cardinality estimators. */
	private final CardinalityEstimator[] cardinalityEstimators;
	
	/** The quality dominances. */
	private final Dominance[] qualityDominances;
	
	/** The dominances. */
	private final Dominance[][] dominances;
	
	/** The validators. */
	private final List<Validator>[] validators;
	
	/**
	 * Instantiates a new multi threaded context.
	 *
	 * @param parallelThreads the parallel threads
	 * @param chaser the chaser
	 * @param detector the detector
	 * @param cardinalityEstimator the cardinality estimator
	 * @param qualityDominance the quality dominance
	 * @param dominance the dominance
	 * @param validators the validators
	 * @throws Exception the exception
	 */
	public MultiThreadedContext(int parallelThreads,
			Chaser chaser,
			HomomorphismDetector detector,
			CardinalityEstimator cardinalityEstimator,
			Dominance qualityDominance,
			Dominance[] dominance,
			List<Validator> validators) throws Exception {
		this.parallelThreads = parallelThreads;
		this.reasoners = new Chaser[this.parallelThreads];
		this.detectors = new HomomorphismDetector[this.parallelThreads];
		this.cardinalityEstimators = new CardinalityEstimator[this.parallelThreads];
		this.qualityDominances = new Dominance[this.parallelThreads];
		this.validators = new List[this.parallelThreads];
		this.dominances = new Dominance[this.parallelThreads][];
				
		for(int p = 0; p < this.parallelThreads; ++p) {
			this.reasoners[p] = (Chaser) chaser.clone();
			this.detectors[p] = detector.clone();
			this.cardinalityEstimators[p] = cardinalityEstimator.clone();
			this.qualityDominances[p] = qualityDominance.clone();
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
	 * Gets the cardinality estimators.
	 *
	 * @return CostEstimator<DAGPlan>[]
	 */
	public CardinalityEstimator[] getCardinalityEstimators() {
		return this.cardinalityEstimators;
	}

	/**
	 * Gets the quality dominances.
	 *
	 * @return the quality dominances
	 */
	public Dominance[] getQualityDominances() {
		return this.qualityDominances;
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
