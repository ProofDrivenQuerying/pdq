package uk.ac.ox.cs.pdq.planner.dag;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.Dominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;

import com.google.common.base.Preconditions;


/**
 * A binary configuration. Corresponds to the join or the cross product of two configurations 
 * @author Efthymia Tsamoura
 *
 */
public class BinaryConfiguration extends DAGChaseConfiguration {

	/**
	 * Binary configuration types
	 * @author Efthymia Tsamoura
	 *
	 */
	public enum BinaryConfigurationTypes {
		PCOMPOSE,
		JCOMPOSE,
		MERGE,
		GENCOMPOSE;
	}

	/** The type of the binary configuration, e.g., Merge, PCompose, JCompose, GenCompose */
	private final BinaryConfigurationTypes type;

	/** The left sub-configuration */
	private final DAGChaseConfiguration left;

	/** The right sub-configuration */
	private final DAGChaseConfiguration right;

	/** The string representation if this configuration*/
	private String toString;
	

	/**
	 * 
	 * @param accessibleSchema
	 * @param query
	 * 		The input query
	 * @param chaser
	 * 		Chase reasoner
	 * @param state
	 * 		The state of this configuration.
	 * @param dominance
	 * 		Perform dominance checks
	 * @param successDominance
	 * 		Performs success dominance checks
	 * @param costEstimator
	 * 		Estimates the configuration's plan
	 * @param left
	 * 		The left sub-configuration
	 * @param right
	 * 		The right sub-configuration
	 * @param chase
	 * 		true if we will chase the configuration's state
	 */
	public BinaryConfiguration(
			AccessibleSchema accessibleSchema,
			Query<?> query,
			Chaser chaser,
			AccessibleChaseState state,
			Dominance[] dominance,
			SuccessDominance successDominance,
			CostEstimator<DAGPlan> costEstimator,
			DAGChaseConfiguration left,
			DAGChaseConfiguration right,	
			Boolean chase) {
		super(accessibleSchema,
				query,
				chaser,
				state != null ? state.clone() : ConfigurationUtility.merge(left, right),
				null,
				ConfigurationUtility.getInput(left, right),
				ConfigurationUtility.getOutput(left, right),
				ConfigurationUtility.arrayCopy(dominance),
				successDominance.clone(),
				left.getHeight() + right.getHeight(),
				ConfigurationUtility.getBushiness(left, right),
				costEstimator);
		Preconditions.checkNotNull(left);
		Preconditions.checkNotNull(right);
		this.left = left;
		this.right = right;
		this.type = ConfigurationUtility.getCombinationType(left, right);
		if (chase) {
			this.chase();
		}
		DAGPlan plan = PlanGenerator.toPlan(this);
		this.setPlan(plan);
		this.getCostEstimator().cost(plan);
		Preconditions.checkState(this.getInput().containsAll(this.getPlan().getInputs()));
		Preconditions.checkState(this.getPlan().getInputs().containsAll(this.getPlan().getInputs()));
	}

	/**
	 * Chases the configuration using the inferred accessible axioms
	 */
	public void chase() {
		this.chaser.reasonUntilTermination(this.getState(), this.getQuery(), this.accessibleSchema.getInferredAccessibilityAxioms());
	}

	/**
	 * @return the type of the binary configuration, e.g., Merge, PCompose, JCompose, GenCompose
	 */
	public BinaryConfigurationTypes getType() {
		return this.type;
	}

	/**
	 * @return the left sub-configuration
	 */
	public DAGChaseConfiguration getLeft() {
		return this.left;
	}

	/**
	 * @return the right sub-configuration
	 */
	public DAGChaseConfiguration getRight() {
		return this.right;
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		if(this.toString == null) {
			this.toString = this.type.toString() +
					"(" + this.left.toString() + "," + this.right.toString() + ")";
		}
		return this.toString;
	}
	
	/**
	 * @return BinaryConfiguration<S>
	 * @see uk.ac.ox.cs.pdq.reasoning.Configuration#clone()
	 */
	@Override
	public BinaryConfiguration clone() {
		return new BinaryConfiguration(
				this.getAccessibleSchema(),
				this.getQuery(),
				this.getChaser(),
				null,
				this.getDominanceDetectors(),
				this.getSuccessDominanceDetector(),
				this.getCostEstimator(),
				this.left.clone(),
				this.right.clone(),
				false);
	}
}
