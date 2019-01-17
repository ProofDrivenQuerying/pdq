package uk.ac.ox.cs.pdq.planner.dag;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.planner.plancreation.PlanCreationUtility;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;


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
				BinaryConfiguration.getInput(left, right),
				BinaryConfiguration.getOutput(left, right),
				left.getHeight() + right.getHeight()
				);
		Preconditions.checkNotNull(left);
		Preconditions.checkNotNull(right);
		this.left = left;
		this.right = right;
		this.plan = PlanCreationUtility.createJoinPlan(left.getPlan(), right.getPlan());
		this.rules = BinaryConfiguration.getApplyRules(this);
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
				BinaryConfiguration.getInput(left, right),
				BinaryConfiguration.getOutput(left, right),
				left.getHeight() + right.getHeight()
				);
		Preconditions.checkNotNull(left);
		Preconditions.checkNotNull(right);
		this.left = left;
		this.right = right;
		this.plan = PlanCreationUtility.createJoinPlan(left.getPlan(), right.getPlan());
		this.rules = BinaryConfiguration.getApplyRules(this);
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

	/**
	 *
	 * @param left the left
	 * @param right the right
	 * @return the output constants of the binary configuration composed from the left and right input configurations
	 */
	public static List<Constant> getOutput(DAGChaseConfiguration left, DAGChaseConfiguration right) {
		List<Constant> output = Lists.newArrayList(left.getOutput());
		output.addAll(right.getOutput());
		return output;
	}

	/**
	 *
	 * @param left the left
	 * @param right the right
	 * @return the input constants of the binary configuration composed from the left and right input configurations
	 */
	public static List<Constant> getInput(DAGChaseConfiguration left, DAGChaseConfiguration right) {
		List<Constant> input = Lists.newArrayList();
		input.addAll(left.getInput());
		List<Constant> in2 = Lists.newArrayList(right.getInput());
		in2.removeAll(left.getOutput());
		input.addAll(in2);
		return input;
	}

	/**
	 * Gets the apply rule configurations that lie within a given configuration.
	 *
	 * @param configuration the configuration
	 * @return 		the ApplyRule sub-configurations of the input configuration
	 */
	public static Collection<ApplyRule> getApplyRules(DAGConfiguration configuration) {
		Collection<ApplyRule> ret = new LinkedHashSet<>();
		if(configuration instanceof BinaryConfiguration) {
			ret.addAll(getApplyRules(((BinaryConfiguration) configuration).getLeft()));
			ret.addAll(getApplyRules(((BinaryConfiguration) configuration).getRight()));
		}
		else 
			ret.add((ApplyRule) configuration);
		return ret;
	}
}
