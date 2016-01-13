package uk.ac.ox.cs.pdq.planner.dag.explorer.parallel;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.dag.BinaryConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.ConfigurationUtility;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClasses;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * Creates new binary configurations
 *
 * @author Efthymia Tsamoura
 */
public class ReasoningThread implements Callable<Boolean> {

	protected final Query<?> query;
	
	protected final Collection<? extends Constraint> dependencies;
	
	/** Performs reasoning. Closes newly created binary configurations*/
	protected final Chaser chaser;

	/** Detects homomorphisms*/
	protected final HomomorphismDetector detector;

	/** Estimates the cost of the plans */
	protected final CostEstimator<DAGPlan> costEstimator;
	
	/** Classes of structurally equivalent configurations*/
	protected final DAGEquivalenceClasses equivalenceClasses;

	/** Map of representatives. For each configuration c = BinConfiguration(c_1,c_2) we create a map from the
		equivalence classes of c and c' to c''. This map helps us reducing the chasing time, i.e.,
		if c'_1 and c'_2 are structurally equivalent to c_1 and c_2, respectively, and
		c = BinConfiguration(c_1,c_2) has already been fully chased,
		then we copy the state of c to the state of c' = BinConfiguration(c'_1,c'_2).*/
	protected final Representative representatives;
	
	/** The depth of the output configurations */
	private final int depth;

	/** The configurations to consider on the left */
	private final Queue<DAGChaseConfiguration> left;

	/** The configurations to consider on the right */
	private final Collection<DAGChaseConfiguration> right;
	
	private final DAGChaseConfiguration best;
	
	private final SuccessDominance successDominance;
	
	/** Validates pairs of configurations to be composed*/
	private final List<Validator> validators;
	
	/** The output configurations*/
	private final Map<Pair<DAGChaseConfiguration, DAGChaseConfiguration>, DAGChaseConfiguration> output;

	/**
	 *
	 * @param depth
	 * 		The depth of the output configurations
	 * @param left
	 * 		The configurations to consider on the left
	 * @param right
	 * 		The configurations to consider on the right
	 * @param chaser
	 * 		Performs reasoning. Closes newly created binary configurations
	 * @param detector
	 * 		Detects homomorphisms
	 * @param costEstimator
	 * 		Estimates the cost of the plans
	 * @param representatives
	 * 		Map of representatives. For each configuration c = BinConfiguration(c_1,c_2) we create a map from the
			equivalence classes of c and c' to c''. This map helps us reducing the chasing time, i.e.,
			if c'_1 and c'_2 are structurally equivalent to c_1 and c_2, respectively,
			and c = BinConfiguration(c_1,c_2) has already been fully chased,
			then we copy the state of c to the state of c' = BinConfiguration(c'_1,c'_2).
	   @param templates
	 * 		Maps each configuration to its constituting ApplyRule configurations. Used to speed up chasing, i.e.,
	 * 		when we are about to create a new binary configuration c''= BinaryConfiguration(c,c')
	 * 		from c and c' and there exists another configuration c^(3) with ApplyRules
	 * 		the ApplyRules of c and c' and c^(3) is already chased then we use c^(3)'s state as the state of c''
	 * @param output
	 * 		The output configurations
	 */
	public ReasoningThread(
			int depth,
			Queue<DAGChaseConfiguration> left,
			Collection<DAGChaseConfiguration> right,
			Query<?> query,
			Collection<? extends Constraint> dependencies,
			Chaser chaser,
			HomomorphismDetector detector,
			CostEstimator<DAGPlan> costEstimator,
			SuccessDominance successDominance,
			DAGChaseConfiguration best,
			List<Validator> validators,
			DAGEquivalenceClasses equivalenceClasses, 
			Representative representatives,
			Map<Pair<DAGChaseConfiguration,DAGChaseConfiguration>,DAGChaseConfiguration> output
			) {
		Preconditions.checkNotNull(successDominance);
		Preconditions.checkArgument(validators != null);
		Preconditions.checkArgument(!validators.isEmpty());
		Preconditions.checkNotNull(query);
		Preconditions.checkNotNull(dependencies);
		Preconditions.checkNotNull(chaser);
		Preconditions.checkNotNull(detector);
		Preconditions.checkNotNull(costEstimator);
		Preconditions.checkNotNull(representatives);
		Preconditions.checkNotNull(equivalenceClasses);

		this.query = query;
		this.dependencies = dependencies;
		this.chaser = chaser;
		this.detector = detector;
		this.costEstimator = costEstimator;
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
	 * @return Boolean
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call() {
		DAGChaseConfiguration left;
		//Poll the next configuration from the left input
		while ((left = this.left.poll()) != null) {
			Preconditions.checkNotNull(this.equivalenceClasses.getEquivalenceClass(left));
			Preconditions.checkState(!this.equivalenceClasses.getEquivalenceClass(left).isEmpty());
			//If it comes from an equivalence class that is not sleeping
			if(!this.equivalenceClasses.getEquivalenceClass(left).isSleeping()) {
				//Select configuration from the right input to combine with
				Collection<DAGChaseConfiguration> selected = this.select(left, this.right, this.equivalenceClasses, this.depth);
				for(DAGChaseConfiguration entry:selected) {
					//If the left configuration participates in the creation of at most topk new binary configurations
					if (!this.output.containsKey(Pair.of(left, entry)) ) {
						DAGChaseConfiguration configuration = this.merge(left, entry);
						//Create a new binary configuration
						this.output.put(Pair.of(left, entry), configuration);
						
					}
				}
			}
		}
		return true;
	}

	private Collection<DAGChaseConfiguration> select(DAGChaseConfiguration left, Collection<DAGChaseConfiguration> right, DAGEquivalenceClasses equivalenceClasses, int depth) {
		Set<DAGChaseConfiguration> selected = Sets.newLinkedHashSet();
		for(DAGChaseConfiguration configuration:right) {
			Preconditions.checkNotNull(equivalenceClasses.getEquivalenceClass(configuration));
			Preconditions.checkState(!equivalenceClasses.getEquivalenceClass(configuration).isEmpty());
			if(!equivalenceClasses.getEquivalenceClass(configuration).isSleeping() &&
					this.validate(left, configuration, depth) &&
					ConfigurationUtility.getPotential(left, configuration, this.best == null ? null : this.best.getPlan(), 
							this.costEstimator, this.successDominance)
					){
				selected.add(configuration);
			}
		}
		return selected;
	}
	
	/**
	 * @param left
	 * @param right
	 * @param depth
	 * @return
	 * 		true if the binary configuration composed from the left and right input configurations passes the validation tests,
	 * 		i.e., satisfies given shape restrictions.
	 * 		If depth > 0, then the corresponding binary configuration must be of the given depth.
	 */
	private boolean validate(DAGChaseConfiguration left, DAGChaseConfiguration right, int depth) {
		return ConfigurationUtility.validate(left, right, this.validators, depth);
	}
	
	/**
	 * @param left
	 * @param right
	 * @return a new binary configuration BinConfiguration(left, right)
	 */
	protected DAGChaseConfiguration merge(DAGChaseConfiguration left, DAGChaseConfiguration right) {
		DAGChaseConfiguration configuration = null;
		//A configuration BinConfiguration(c,c'), where c and c' belong to the equivalence classes of
		//the left and right input configuration, respectively.
		DAGChaseConfiguration representative = this.representatives.getRepresentative(this.equivalenceClasses, left, right);
		
		if(representative == null) {
			representative = this.representatives.getRepresentative(this.equivalenceClasses, right, left);
		}
		
		//If the representative is null or we do not use templates or we cannot find a template configuration that
		//consists of the corresponding ApplyRules, then create a binary configuration from scratch by fully chasing its state
		if(representative == null) {
			configuration = new BinaryConfiguration(
					left,
					right
					);
					
			if(configuration.getState() instanceof DatabaseChaseState) {
				((DatabaseChaseState)configuration.getState()).setManager((DBHomomorphismManager) this.detector);
			}
			this.chaser.reasonUntilTermination(configuration.getState(), this.query, this.dependencies);
			this.representatives.put(this.equivalenceClasses, left, right, configuration);

		}
		//otherwise, re-use the state of the representative
		else if(representative != null) {
			configuration = new BinaryConfiguration(
					left,
					right,
					representative.getState().clone()
					);
		}
		this.costEstimator.cost(configuration.getPlan());
		return configuration;
	}
}
