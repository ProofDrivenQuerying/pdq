package uk.ac.ox.cs.pdq.planner.dag.parallel;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.Representative;
import uk.ac.ox.cs.pdq.planner.dag.explorer.Template;
import uk.ac.ox.cs.pdq.planner.dag.priority.PriorityAssessor;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

import com.google.common.base.Preconditions;

/**
 * Creates new binary configurations
 *
 * @author Efthymia Tsamoura
 */
public class ChaseOrPropagateThread extends DefaultThread implements Callable<Boolean> {

	/** The depth of the output configurations */
	private final int depth;

	/** The configurations to consider on the left */
	private final Queue<DAGChaseConfiguration> left;

	/** The configurations to consider on the right */
	private final Collection<DAGChaseConfiguration> right;

	/** Prioritises pairs of configurations */
	private final PriorityAssessor priority;

	/** The output configurations*/
	private final Map<Pair<DAGChaseConfiguration, DAGChaseConfiguration>, DAGChaseConfiguration> output;

	/** Counts the number of times each input configuration was used to create a new binary configuration.
		Used when having topk restrictions */
	private final Map<DAGChaseConfiguration, Integer> participatesIn;


	/**
	 *
	 * @param depth
	 * 		The depth of the output configurations
	 * @param left
	 * 		The configurations to consider on the left
	 * @param right
	 * 		The configurations to consider on the right
	 * @param priority
	 * 		Prioritises pairs of configurations
	 * @param reasoner
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
	 * @param participatesIn
	 * 		Counts the number of times each input configuration was used to create a new binary configuration.
			Used when having topk restrictions
	 */
	public ChaseOrPropagateThread(
			int depth,
			Queue<DAGChaseConfiguration> left,
			Collection<DAGChaseConfiguration> right,
			PriorityAssessor priority,
			Chaser reasoner,
			HomomorphismDetector detector,
			CostEstimator<DAGPlan> costEstimator,
			Representative representatives,
			Template templates,
			Map<Pair<DAGChaseConfiguration,DAGChaseConfiguration>,DAGChaseConfiguration> output,
			Map<DAGChaseConfiguration, Integer> participatesIn
			) {
		super(reasoner, detector, costEstimator, representatives, templates);
		this.depth = depth;
		this.left = left;
		this.right = right;
		this.priority = priority;
		this.output = output;
		this.participatesIn = participatesIn;
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
			Preconditions.checkNotNull(left.getEquivalenceClass());
			Preconditions.checkState(!left.getEquivalenceClass().isEmpty());
			//If it comes from an equivalence class that is not sleeping
			if(!left.getEquivalenceClass().isSleeping()) {
				//Select configuration from the right input to combine with
				Collection<DAGChaseConfiguration> selected = this.priority.select(left, this.right, this.depth);
				for(DAGChaseConfiguration entry:selected) {
					//If the left configuration participates in the creation of at most topk new binary configurations
					if ((this.participatesIn.get(left) == null || this.participatesIn.get(left) < this.priority.getTopk()) &&
							!this.output.containsKey(Pair.of(left, entry)) ) {
						DAGChaseConfiguration configuration = this.merge(left, entry);
						//Create a new binary configuration
						this.output.put(Pair.of(left, entry), configuration);
						this.participatesIn.put(left, this.participatesIn.get(left) == null ? 1 : this.participatesIn.get(left) + 1);
					}
				}
			}
		}
		return true;
	}

}
