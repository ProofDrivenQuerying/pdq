/*
 * 
 */
package uk.ac.ox.cs.pdq.planner.dag;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


// TODO: Auto-generated Javadoc
/**
 * A binary configuration. Corresponds to the join or the cross product of two configurations 
 * @author Efthymia Tsamoura
 *
 */
public class BinaryAnnotatedPlan extends DAGAnnotatedPlan {

	/**  The left sub-configuration. */
	private final DAGAnnotatedPlan left;

	/**  The right sub-configuration. */
	private final DAGAnnotatedPlan right;

	/**  The string representation if this configuration. */
	private String toString;
	
	/**  The configuration's ApplyRule sub-configurations. */
	private final Collection<UnaryAnnotatedPlan> rules;

	/**  The configuration's ApplyRule sub-configurations ordered according to their appearance. */
	private final List<UnaryAnnotatedPlan> rulesList;
	
	/** The constants of this annotated plan that appear on the facts used to build up the constituting unary annotated plans.
	 */
	private final Collection<Constant> exportedConstants;
	
	/**
	 * Instantiates a new binary annotated plan.
	 *
	 * @param left 		The left sub-configuration
	 * @param right 		The right sub-configuration
	 */
	public BinaryAnnotatedPlan(
			DAGAnnotatedPlan left,
			DAGAnnotatedPlan right
			) {
		super(ConfigurationUtility.merge(left, right),
				ConfigurationUtility.getOutput(left, right),
				left.getHeight() + right.getHeight(),
				ConfigurationUtility.getBushiness(left, right)
				);
		Preconditions.checkNotNull(left);
		Preconditions.checkNotNull(right);
		this.left = left;
		this.right = right;
		this.rules = ConfigurationUtility.getUnaryAnnotatedPlans(this);
		this.rulesList = ConfigurationUtility.getUnaryAnnotatedPlansList(this);
		
		this.exportedConstants = Sets.newHashSet();
		for(UnaryAnnotatedPlan rule:this.rules) {
			this.exportedConstants.addAll(Utility.getConstants(rule.getFact())) ;
		}
	}
	
	/**
	 * Instantiates a new binary annotated plan.
	 *
	 * @param left 		The left sub-configuration
	 * @param right 		The right sub-configuration
	 * @param state 		The chase state of the new annotated plan
	 */
	public BinaryAnnotatedPlan(
			DAGAnnotatedPlan left,
			DAGAnnotatedPlan right,
			ChaseState state
			) {
		super(state,
				ConfigurationUtility.getOutput(left, right),
				left.getHeight() + right.getHeight(),
				ConfigurationUtility.getBushiness(left, right)
				);
		Preconditions.checkNotNull(left);
		Preconditions.checkNotNull(right);
		this.left = left;
		this.right = right;
		this.rules = ConfigurationUtility.getUnaryAnnotatedPlans(this);
		this.rulesList = ConfigurationUtility.getUnaryAnnotatedPlansList(this);
		
		this.exportedConstants = Sets.newHashSet();
		for(UnaryAnnotatedPlan rule:this.rules) {
			this.exportedConstants.addAll(Utility.getConstants(rule.getFact())) ;
		}
	}

	/**
	 * Chases the configuration using the input dependencies.
	 *
	 * @param chaser the chaser
	 * @param query the query
	 * @param dependencies the dependencies
	 */
	public void reasonUntilTermination(Chaser chaser, Query<?> query, Collection<? extends Constraint> dependencies) {
		chaser.reasonUntilTermination(this.getState(), dependencies);
	}

	/**
	 * Gets the left.
	 *
	 * @return the left sub-configuration
	 */
	public DAGAnnotatedPlan getLeft() {
		return this.left;
	}

	/**
	 * Gets the right.
	 *
	 * @return the right sub-configuration
	 */
	public DAGAnnotatedPlan getRight() {
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
			this.toString = "Binary" +
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
	public DAGAnnotatedPlan clone() {
		BinaryAnnotatedPlan clone = new BinaryAnnotatedPlan(
				this.left.clone(),
				this.right.clone()
				);
		clone.setSize(this.getSize());
		clone.setCardinality(this.getCardinality());
		clone.setQuality(this.getQuality());
		clone.setAdjustedQuality(this.getAdjustedQuality());
		return clone;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.dag.DAGConfiguration#getUnaryAnnotatedPlans()
	 */
	@Override
	public Collection<UnaryAnnotatedPlan> getUnaryAnnotatedPlans() {
		return this.rules;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.dag.DAGConfiguration#getUnaryAnnotatedPlansList()
	 */
	@Override
	public List<UnaryAnnotatedPlan> getUnaryAnnotatedPlansList() {
		return this.rulesList;
	}

	/**
	 * Gets the exported constants.
	 *
	 * @return the constants of this annotated plan that appear on the facts
	 * used to build up the constituting unary annotated plans.
	 * The new chase constants that are produced during chasing are not returned.
	 */
	@Override
	public Collection<Constant> getExportedConstants() {
		return this.exportedConstants;
	}
}
