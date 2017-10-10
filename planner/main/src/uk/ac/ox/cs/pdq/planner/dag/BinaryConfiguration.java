package uk.ac.ox.cs.pdq.planner.dag;

import java.util.Collection;
import java.util.List;



import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance;
import uk.ac.ox.cs.pdq.planner.util.PlanCreationUtility;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;

import com.google.common.base.Preconditions;


/**
 * Configurations represent a place in the search space for plans and proofs. In the DAG world these are built up inductively, and binary configurations are the main inductive constructor. Instances of binary DAG configurations.
 * They are of the form Binary(x,y), where x and y can be either binary of unary DAG configurations. 
 * Binary(x,y) has input I1 \cup (I2-O1) and output O1 \cup O2, and output facts all facts that are consequences of
 * the union of the facts in x and y under the copy of the integrity constraints on the InfAcc relations. 
 * Similar to unary DAG configurations, calculating the set of facts requires the use of consequence closure.
 * @author Efthymia Tsamoura
 *
 */
public class BinaryConfiguration extends DAGChaseConfiguration {

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
		super(left.getState().merge(right.getState()),
				ConfigurationUtility.getInput(left, right),
				ConfigurationUtility.getOutput(left, right),
				left.getHeight() + right.getHeight(),
				ConfigurationUtility.getBushiness(left, right)
				);
		Preconditions.checkNotNull(left);
		Preconditions.checkNotNull(right);
		this.left = left;
		this.right = right;
		this.plan = PlanCreationUtility.createPlan(left.getPlan(), right.getPlan());
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
			AccessibleChaseInstance state
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
		this.plan = PlanCreationUtility.createPlan(left.getPlan(), right.getPlan());
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
	public void reasonUntilTermination(Chaser chaser, ConjunctiveQuery query, Dependency[] dependencies) {
		chaser.reasonUntilTermination(this.getState(), dependencies);
	}

	/**
	 *
	 * @return the left sub-configuration
	 */
	public DAGChaseConfiguration getLeft() {
		return this.left;
	}

	/**
	 *
	 * @return the right sub-configuration
	 */
	public DAGChaseConfiguration getRight() {
		return this.right;
	}

	/**
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		if(this.toString == null) {
			this.toString = "BINARY"+
					"(" + this.left.toString() + "," + this.right.toString() + ")";
		}
		return this.toString;
	}
	
	/**
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
