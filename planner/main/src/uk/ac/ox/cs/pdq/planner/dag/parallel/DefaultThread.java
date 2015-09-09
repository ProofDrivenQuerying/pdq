package uk.ac.ox.cs.pdq.planner.dag.parallel;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.dag.BinaryConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.Representative;
import uk.ac.ox.cs.pdq.planner.dag.explorer.Template;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

import com.google.common.base.Preconditions;

/**
 * Provides a function to speed up binary configuration creation either through the use of representative or template configurations.
 *
 * @author Efthymia Tsamoura
 *
 * @param 
 */
public abstract class DefaultThread extends ExecutionThread {

	/** Performs reasoning. Closes newly created binary configurations*/
	protected final Chaser reasoner;

	/** Detects homomorphisms*/
	protected final HomomorphismDetector detector;

	/** Estimates the cost of the plans */
	protected final CostEstimator<DAGPlan> costEstimator;

	/** Map of representatives. For each configuration c = BinConfiguration(c_1,c_2) we create a map from the
		equivalence classes of c and c' to c''. This map helps us reducing the chasing time, i.e.,
		if c'_1 and c'_2 are structurally equivalent to c_1 and c_2, respectively, and
		c = BinConfiguration(c_1,c_2) has already been fully chased,
		then we copy the state of c to the state of c' = BinConfiguration(c'_1,c'_2).*/
	protected final Representative representatives;

	/**
	 * Maps each configuration to its constituting ApplyRule configurations. Used to speed up chasing, i.e.,
	 * when we are about to create a new binary configuration c''= BinaryConfiguration(c,c')
	 * from c and c' and there exists another configuration c^(3) with ApplyRules
	 * the ApplyRules of c and c' and c^(3) is already chased then we use c^(3)'s state as the state of c''
	 */
	protected final Template templates;

	/**
	 *
	 * @param reasoner
	 * 		Performs reasoning. Closes newly created binary configurations
	 * @param detector
	 * 		Detects homomorphisms
	 * @param costEstimator
	 * 		Estimates the cost of the plans
	 * @param representatives
	 * 		Map of representatives. For each configuration c = BinConfiguration(c_1,c_2) we create a map from the
			equivalence classes of c and c' to c''. This map helps us reducing the chasing time, i.e.,
			if c'_1 and c'_2 are structurally equivalent to c_1 and c_2, respectively, and
			c = BinConfiguration(c_1,c_2) has already been fully chased,
			then we copy the state of c to the state of c' = BinConfiguration(c'_1,c'_2).
	 * @param templates
	 * 		Maps each configuration to its constituting ApplyRule configurations. Used to speed up chasing, i.e.,
	 * 		when we are about to create a new binary configuration c''= BinaryConfiguration(c,c')
	 * 		from c and c' and there exists another configuration c^(3) with ApplyRules
	 * 		the ApplyRules of c and c' and c^(3) is already chased then we use c^(3)'s state as the state of c''
	 */
	public DefaultThread(
			Chaser reasoner,
			HomomorphismDetector detector,
			CostEstimator<DAGPlan> costEstimator,
			Representative representatives,
			Template templates
			) {
		Preconditions.checkNotNull(reasoner);
		Preconditions.checkNotNull(detector);
		Preconditions.checkNotNull(costEstimator);
		Preconditions.checkNotNull(representatives);

		this.reasoner = reasoner;
		this.detector = detector;
		this.costEstimator = costEstimator;
		this.representatives = representatives;
		this.templates = templates;
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
		DAGChaseConfiguration representative = this.representatives.getRepresentative(left, right);
		
		DAGChaseConfiguration template = null;
		if(this.templates != null) {
			template = this.templates.getTemplate(left, right);
		}
		
		if(representative == null) {
			representative = this.representatives.getRepresentative(right, left);
		}
		
		//If the representative is null or we do not use templates or we cannot find a template configuration that
		//consists of the corresponding ApplyRules, then create a binary configuration from scratch by fully chasing its state
		if(representative == null && (this.templates == null || template == null)) {
			configuration = new BinaryConfiguration(
					left.getAccessibleSchema(),
					left.getQuery(),
					(Chaser) this.reasoner,
					null,
					left.getDominanceDetectors(),
					left.getSuccessDominanceDetector(),
					this.costEstimator,
					left,
					right,
					false);
					
			if(configuration.getState() instanceof DatabaseChaseState) {
				((DatabaseChaseState)configuration.getState()).setManager((DBHomomorphismManager) this.detector);
			}
			this.reasoner.reasonUntilTermination(configuration.getState(), configuration.getQuery(), left.getAccessibleSchema().getInferredAccessibilityAxioms());
	
			if(this.templates != null) {
				this.templates.put(configuration);
			}
			this.representatives.put(left, right, configuration);

		}
		//otherwise, re-use the state of the representative
		else if(representative != null) {
			configuration = new BinaryConfiguration(
					left.getAccessibleSchema(),
					left.getQuery(),
					(Chaser) this.reasoner,
					null,
					left.getDominanceDetectors(),
					left.getSuccessDominanceDetector(),
					this.costEstimator,
					left,
					right,
					false);

			if(this.templates != null && template == null) {
				this.templates.put(configuration);
			}
		}
		//or the template configuration
		else if(this.templates != null && template != null) {
			representative = template;
			configuration = new BinaryConfiguration(
					left.getAccessibleSchema(),
					left.getQuery(),
					(Chaser) this.reasoner,
					null,
					left.getDominanceDetectors(),
					left.getSuccessDominanceDetector(),
					this.costEstimator,
					left,
					right,
					false);

			this.representatives.put(left, right, configuration);
		}
		return configuration;
	}
}