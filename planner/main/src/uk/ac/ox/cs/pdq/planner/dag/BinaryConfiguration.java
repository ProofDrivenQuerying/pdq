package uk.ac.ox.cs.pdq.planner.dag;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;

import com.google.common.base.Preconditions;


// TODO: Auto-generated Javadoc
/**
 * Instances of binary DAG configurations.
 * They are of the form Binary(x,y), where x and y can be either binary of unary DAG configurations. 
 * Binary(x,y) has input I1 \cup (I2-O1) and output O1 \cup O2, and output facts all facts that are consequences of
 * the union of the facts in x and y under the copy of the integrity constraints on the InfAcc relations. 
 * Similar to unary DAG configurations, calculating the set of facts requires the use of consequence closure.
 * @author Efthymia Tsamoura
 *
 */
public class BinaryConfiguration extends DAGChaseConfiguration {

	/**
	 * Binary configuration types.
	 *
	 * @author Efthymia Tsamoura
	 */
	public enum BinaryConfigurationTypes {
		
		/** The pcompose. */
		PCOMPOSE,
		
		/** The jcompose. */
		JCOMPOSE,
		
		/** The merge. */
		MERGE,
		
		/** The gencompose. */
		GENCOMPOSE;
	}

	/** The type of the binary configuration, e.g., Merge, PCompose, JCompose, GenCompose */
	private final BinaryConfigurationTypes type;

	/**  The left sub-configuration. */
	private final DAGChaseConfiguration left;

	/**  The right sub-configuration. */
	private final DAGChaseConfiguration right;

	/**  The string representation if this configuration. */
	private String toString;
	
	/**  The configuration's ApplyRule sub-configurations. */
	private final Collection<ApplyRule> rules;

	/**  The configuration's ApplyRule sub-configurations ordered according to their appearance. */
	private final List<ApplyRule> rulesList;
	
	/**
	 * Instantiates a new binary configuration.
	 *
	 * @param left 		The left sub-configuration
	 * @param right 		The right sub-configuration
	 */
	public BinaryConfiguration(
			DAGChaseConfiguration left,
			DAGChaseConfiguration right
			) {
		super(ConfigurationUtility.merge(left, right),
				ConfigurationUtility.getInput(left, right),
				ConfigurationUtility.getOutput(left, right),
				left.getHeight() + right.getHeight(),
				ConfigurationUtility.getBushiness(left, right)
				);
		Preconditions.checkNotNull(left);
		Preconditions.checkNotNull(right);
		this.left = left;
		this.right = right;
		this.type = ConfigurationUtility.getCombinationType(left, right);
		DAGPlan plan = DAGPlanGenerator.toDAGPlan(this);
		this.setPlan(plan);
		Preconditions.checkState(this.getInput().containsAll(this.getPlan().getInputs()));
		Preconditions.checkState(this.getPlan().getInputs().containsAll(this.getPlan().getInputs()));
		this.rules = ConfigurationUtility.getApplyRules(this);
		this.rulesList = ConfigurationUtility.getApplyRulesList(this);
	}
	
	/**
	 * Instantiates a new binary configuration.
	 *
	 * @param left 		The left sub-configuration
	 * @param right 		The right sub-configuration
	 * @param state 		The state of the new binary configuration
	 */
	public BinaryConfiguration(
			DAGChaseConfiguration left,
			DAGChaseConfiguration right,
			AccessibleChaseState state
			) {
		super(state,
				ConfigurationUtility.getInput(left, right),
				ConfigurationUtility.getOutput(left, right),
				left.getHeight() + right.getHeight(),
				ConfigurationUtility.getBushiness(left, right)
				);
		Preconditions.checkNotNull(left);
		Preconditions.checkNotNull(right);
		this.left = left;
		this.right = right;
		this.type = ConfigurationUtility.getCombinationType(left, right);
		DAGPlan plan = DAGPlanGenerator.toDAGPlan(this);
		this.setPlan(plan);
		Preconditions.checkState(this.getInput().containsAll(this.getPlan().getInputs()));
		Preconditions.checkState(this.getPlan().getInputs().containsAll(this.getPlan().getInputs()));
		this.rules = ConfigurationUtility.getApplyRules(this);
		this.rulesList = ConfigurationUtility.getApplyRulesList(this);
	}

	/**
	 * Chases the configuration using the input dependencies.
	 *
	 * @param chaser the chaser
	 * @param query the query
	 * @param dependencies the dependencies
	 */
	public void reasonUntilTermination(Chaser chaser, ConjunctiveQuery query, Collection<? extends Dependency> dependencies) {
		chaser.reasonUntilTermination(this.getState(), dependencies);
	}

	/**
	 * Gets the type.
	 *
	 * @return the type of the binary configuration, e.g., Merge, PCompose, JCompose, GenCompose
	 */
	public BinaryConfigurationTypes getType() {
		return this.type;
	}

	/**
	 * Gets the left.
	 *
	 * @return the left sub-configuration
	 */
	public DAGChaseConfiguration getLeft() {
		return this.left;
	}

	/**
	 * Gets the right.
	 *
	 * @return the right sub-configuration
	 */
	public DAGChaseConfiguration getRight() {
		return this.right;
	}

	/**
	 * To string.
	 *
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
	 * Clone.
	 *
	 * @return BinaryConfiguration<S>
	 * @see uk.ac.ox.cs.pdq.reasoning.Configuration#clone()
	 */
	@Override
	public BinaryConfiguration clone() {
		return new BinaryConfiguration(
				this.left.clone(),
				this.right.clone()
				);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.dag.DAGConfiguration#getApplyRules()
	 */
	public Collection<ApplyRule> getApplyRules() {
		return this.rules;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.dag.DAGConfiguration#getApplyRulesList()
	 */
	public List<ApplyRule> getApplyRulesList() {
		return this.rulesList;
	}
}
