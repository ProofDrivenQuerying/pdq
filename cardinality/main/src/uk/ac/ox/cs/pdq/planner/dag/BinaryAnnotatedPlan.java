package uk.ac.ox.cs.pdq.planner.dag;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;

import com.google.common.base.Preconditions;


/**
 * A binary configuration. Corresponds to the join or the cross product of two configurations 
 * @author Efthymia Tsamoura
 *
 */
public class BinaryAnnotatedPlan extends DAGAnnotatedPlan {

	/** The left sub-configuration */
	private final DAGAnnotatedPlan left;

	/** The right sub-configuration */
	private final DAGAnnotatedPlan right;

	/** The string representation if this configuration*/
	private String toString;
	
	/** The configuration's ApplyRule sub-configurations */
	private final Collection<UnaryAnnotatedPlan> rules;

	/** The configuration's ApplyRule sub-configurations ordered according to their appearance */
	private final List<UnaryAnnotatedPlan> rulesList;
	
	/**
	 * 
	 * @param left
	 * 		The left sub-configuration
	 * @param right
	 * 		The right sub-configuration
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
	}

	/**
	 * Chases the configuration using the input dependencies
	 */
	public void chase(Chaser chaser, Query<?> query, Collection<? extends Constraint> dependencies) {
		chaser.reasonUntilTermination(this.getState(), query, dependencies);
	}

	/**
	 * @return the left sub-configuration
	 */
	public DAGAnnotatedPlan getLeft() {
		return this.left;
	}

	/**
	 * @return the right sub-configuration
	 */
	public DAGAnnotatedPlan getRight() {
		return this.right;
	}

	/**
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
	 * @return BinaryConfiguration<S>
	 * @see uk.ac.ox.cs.pdq.reasoning.Configuration#clone()
	 */
	@Override
	public DAGAnnotatedPlan clone() {
		return new BinaryAnnotatedPlan(
				this.left.clone(),
				this.right.clone()
				);
	}

	@Override
	public Collection<UnaryAnnotatedPlan> getUnaryAnnotatedPlans() {
		return this.rules;
	}

	@Override
	public List<UnaryAnnotatedPlan> getUnaryAnnotatedPlansList() {
		return this.rulesList;
	}
}
