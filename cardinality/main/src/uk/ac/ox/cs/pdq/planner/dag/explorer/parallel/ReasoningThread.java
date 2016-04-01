/*
 * 
 */
package uk.ac.ox.cs.pdq.planner.dag.explorer.parallel;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.planner.cardinality.CardinalityEstimator;
import uk.ac.ox.cs.pdq.planner.dag.BinaryAnnotatedPlan;
import uk.ac.ox.cs.pdq.planner.dag.ConfigurationUtility;
import uk.ac.ox.cs.pdq.planner.dag.DAGAnnotatedPlan;
import uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGAnnotatedPlanClasses;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.Dominance;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

// TODO: Auto-generated Javadoc
/**
 * Creates new binary configurations.
 *
 * @author Efthymia Tsamoura
 */
public class ReasoningThread implements Callable<Boolean> {

	/** The log. */
	protected static Logger log = Logger.getLogger(ReasoningThread.class);

	/** The query. */
	protected final Query<?> query;

	/** The schema. */
	protected final Schema schema;

	/** Performs reasoning. Closes newly created binary configurations*/
	protected final Chaser chaser;

	/**  Detects homomorphisms. */
	protected final HomomorphismDetector detector;

	/**  Estimates the cost of the plans. */
	protected final CardinalityEstimator cardinalityEstimator;

	/**  Classes of structurally equivalent configurations. */
	protected final DAGAnnotatedPlanClasses equivalenceClasses;

	/** Map of representatives. For each configuration c = BinConfiguration(c_1,c_2) we create a map from the
		equivalence classes of c and c' to c''. This map helps us reducing the chasing time, i.e.,
		if c'_1 and c'_2 are structurally equivalent to c_1 and c_2, respectively, and
		c = BinConfiguration(c_1,c_2) has already been fully chased,
		then we copy the state of c to the state of c' = BinConfiguration(c'_1,c'_2).*/
	protected final AnnotatedPlanRepresentative representatives;

	/**  The depth of the output configurations. */
	private final int depth;

	/**  The configurations to consider on the left. */
	private final Queue<DAGAnnotatedPlan> left;

	/**  The configurations to consider on the right. */
	private final Collection<DAGAnnotatedPlan> right;

	/** The best. */
	private final DAGAnnotatedPlan best;

	/** The success dominance. */
	private final Dominance<DAGAnnotatedPlan> successDominance;

	/**  Validates pairs of configurations to be composed. */
	private final List<Validator> validators;

	/**  The output configurations. */
	private final Map<Pair<DAGAnnotatedPlan, DAGAnnotatedPlan>, DAGAnnotatedPlan> output;

	/**
	 * Instantiates a new reasoning thread.
	 *
	 * @param depth 		The depth of the output configurations
	 * @param left 		The configurations to consider on the left
	 * @param right 		The configurations to consider on the right
	 * @param query the query
	 * @param schema the schema
	 * @param chaser 		Performs reasoning. Closes newly created binary configurations
	 * @param detector 		Detects homomorphisms
	 * @param cardinalityEstimator 		Estimates the cost of the plans
	 * @param successDominance the success dominance
	 * @param best the best
	 * @param validators the validators
	 * @param equivalenceClasses the equivalence classes
	 * @param representatives 		Map of representatives. For each configuration c = BinConfiguration(c_1,c_2) we create a map from the
	 * 			equivalence classes of c and c' to c''. This map helps us reducing the chasing time, i.e.,
	 * 			if c'_1 and c'_2 are structurally equivalent to c_1 and c_2, respectively,
	 * 			and c = BinConfiguration(c_1,c_2) has already been fully chased,
	 * 			then we copy the state of c to the state of c' = BinConfiguration(c'_1,c'_2).
	 * @param output 		The output configurations
	 */
	public ReasoningThread(
			int depth,
			Queue<DAGAnnotatedPlan> left,
			Collection<DAGAnnotatedPlan> right,
			Query<?> query,
			Schema schema,
			Chaser chaser,
			HomomorphismDetector detector,
			CardinalityEstimator cardinalityEstimator,
			Dominance<DAGAnnotatedPlan> successDominance,
			DAGAnnotatedPlan best,
			List<Validator> validators,
			DAGAnnotatedPlanClasses equivalenceClasses, 
			AnnotatedPlanRepresentative representatives,
			Map<Pair<DAGAnnotatedPlan,DAGAnnotatedPlan>,DAGAnnotatedPlan> output
			) {
		Preconditions.checkNotNull(successDominance);
		Preconditions.checkArgument(validators != null);
		Preconditions.checkArgument(!validators.isEmpty());
		Preconditions.checkNotNull(query);
		Preconditions.checkNotNull(schema);
		Preconditions.checkNotNull(chaser);
		Preconditions.checkNotNull(detector);
		Preconditions.checkNotNull(cardinalityEstimator);
		Preconditions.checkNotNull(representatives);
		Preconditions.checkNotNull(equivalenceClasses);

		this.query = query;
		this.schema = schema;
		this.chaser = chaser;
		this.detector = detector;
		this.cardinalityEstimator = cardinalityEstimator;
		this.equivalenceClasses = equivalenceClasses;
		this.representatives = representatives;
		this.validators = validators;
		this.depth = depth;
		this.left = left;
		this.right = right;
		this.best = best;
		this.successDominance = successDominance;
		this.output = output;
	}

	/**
	 * Call.
	 *
	 * @return Boolean
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call() {
		DAGAnnotatedPlan left;
		//Poll the next configuration from the left input
		while ((left = this.left.poll()) != null) {
			Preconditions.checkNotNull(this.equivalenceClasses.getEquivalenceClass(left));
			Preconditions.checkState(!this.equivalenceClasses.getEquivalenceClass(left).isEmpty());
			//If it comes from an equivalence class that is not sleeping
			{
				//Select configuration from the right input to combine with
				Collection<DAGAnnotatedPlan> selected = this.select(left, this.right, this.equivalenceClasses, this.depth);
				for(DAGAnnotatedPlan entry:selected) {
					//If the left configuration participates in the creation of at most topk new binary configurations
					if (!this.output.containsKey(Pair.of(left, entry)) ) {
						if(ConfigurationUtility.merge(left, entry) != null) {

							DAGAnnotatedPlan configuration = this.merge(left, entry);
							//Create a new binary configuration
							this.output.put(Pair.of(left, entry), configuration);

						}						
					}
				}
			}
		}
		return true;
	}

	/**
	 * Select.
	 *
	 * @param left the left
	 * @param right the right
	 * @param equivalenceClasses the equivalence classes
	 * @param depth the depth
	 * @return the collection
	 */
	private Collection<DAGAnnotatedPlan> select(DAGAnnotatedPlan left, Collection<DAGAnnotatedPlan> right, DAGAnnotatedPlanClasses equivalenceClasses, int depth) {
		Set<DAGAnnotatedPlan> selected = Sets.newLinkedHashSet();
		for(DAGAnnotatedPlan configuration:right) {
			Preconditions.checkNotNull(equivalenceClasses.getEquivalenceClass(configuration));
			Preconditions.checkState(!equivalenceClasses.getEquivalenceClass(configuration).isEmpty());
			if(this.validate(left, configuration, depth)){
				selected.add(configuration);
			}
		}
		return selected;
	}

	/**
	 * Validate.
	 *
	 * @param left the left
	 * @param right the right
	 * @param depth the depth
	 * @return 		true if the binary configuration composed from the left and right input configurations passes the validation tests,
	 * 		i.e., satisfies given shape restrictions.
	 * 		If depth > 0, then the corresponding binary configuration must be of the given depth.
	 */
	private boolean validate(DAGAnnotatedPlan left, DAGAnnotatedPlan right, int depth) {
		return ConfigurationUtility.validate(left, right, this.validators, depth);
	}

	/**
	 * Merge.
	 *
	 * @param left the left
	 * @param right the right
	 * @return a new binary configuration BinConfiguration(left, right)
	 */
	protected DAGAnnotatedPlan merge(DAGAnnotatedPlan left, DAGAnnotatedPlan right) {	
		DAGAnnotatedPlan configuration = null;
		//A configuration BinConfiguration(c,c'), where c and c' belong to the equivalence classes of
		//the left and right input configuration, respectively.
		DAGAnnotatedPlan representative = this.representatives.getRepresentative(this.equivalenceClasses, left, right);	

		if(representative == null) {
			representative = this.representatives.getRepresentative(this.equivalenceClasses, right, left);
		}
		//If the representative is null or we do not use templates or we cannot find a template configuration that
		//consists of the corresponding ApplyRules, then create a binary configuration from scratch by fully chasing its state
		if(representative == null) {
			log.trace("No representative found for input " + left + " " + right);
			configuration = new BinaryAnnotatedPlan(left,right);				
			if(configuration.getState() instanceof DatabaseChaseState) {
				((DatabaseChaseState)configuration.getState()).setManager((DBHomomorphismManager) this.detector);
			}
			this.chaser.reasonUntilTermination(configuration.getState(),
					CollectionUtils.union(this.schema.getDependencies(), this.schema.getKeyDependencies()));
			Pair<BigInteger, Double> sizeQuality = this.cardinalityEstimator.sizeQualityOf(left, right, this.chaser, this.detector, 
					this.schema.getKeyDependencies());
			configuration.setSize(sizeQuality.getLeft());
			configuration.setQuality(sizeQuality.getRight());
			//Estimate the configuration's cardinality (size after considering any projections)
			BigInteger cardinality = this.cardinalityEstimator.cardinalityOf(configuration, this.query);
			configuration.setCardinality(cardinality);					
			this.representatives.put(this.equivalenceClasses, left, right, configuration);
			
		}
		//otherwise, re-use the state of the representative
		else if(representative != null) {
			log.trace("Found representative " + representative + " for input " + left + " " + right);
			configuration = new BinaryAnnotatedPlan(left,right, representative.getState().clone());			
			Pair<BigInteger, Double> sizeQuality = this.cardinalityEstimator.sizeQualityOf(left, right, this.chaser, this.detector, 
					this.schema.getKeyDependencies());
			configuration.setSize(sizeQuality.getLeft());
			configuration.setQuality(sizeQuality.getRight());	
			//Estimate the configuration's cardinality (size after considering any projections)
			BigInteger cardinality = this.cardinalityEstimator.cardinalityOf(configuration, this.query);
			configuration.setCardinality(cardinality);					
		}		
		return configuration;
	}
}
