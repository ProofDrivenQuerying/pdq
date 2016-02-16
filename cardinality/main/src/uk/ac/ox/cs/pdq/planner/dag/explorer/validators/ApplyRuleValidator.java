/*
 * 
 */
package uk.ac.ox.cs.pdq.planner.dag.explorer.validators;

import uk.ac.ox.cs.pdq.planner.dag.ConfigurationUtility;
import uk.ac.ox.cs.pdq.planner.dag.DAGAnnotatedPlan;
import uk.ac.ox.cs.pdq.planner.dag.DAGConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.UnaryAnnotatedPlan;


// TODO: Auto-generated Javadoc
/**
 * Requires the input pair of configurations to be non trivial
 * and at least one of the input configurations to be an UnaryAnnotatedPlan.
 *
 * @author Efthymia Tsamoura
 */
public class ApplyRuleValidator implements Validator{

	/**
	 * Instantiates a new apply rule validator.
	 */
	public ApplyRuleValidator() {
	}

	/**
	 * Validate.
	 *
	 * @param left DAGConfiguration
	 * @param right DAGConfiguration
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.dag.explorer.validators.Validator#validate(DAGConfiguration, DAGConfiguration)
	 */
	@Override
	public boolean validate(DAGAnnotatedPlan left, DAGAnnotatedPlan right) {
		return (left instanceof UnaryAnnotatedPlan || right instanceof UnaryAnnotatedPlan)
				&& ConfigurationUtility.isNonTrivial(left, right);
	}

	/**
	 * Validate.
	 *
	 * @param left DAGConfiguration
	 * @param right DAGConfiguration
	 * @param depth int
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.dag.explorer.validators.Validator#validate(DAGConfiguration, DAGConfiguration, int)
	 */
	@Override
	public boolean validate(DAGAnnotatedPlan left, DAGAnnotatedPlan right, int depth) {
		return left.getHeight() + right.getHeight() == depth && this.validate(left, right);
	}

	/**
	 * Clone.
	 *
	 * @return Validator
	 * @see uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator#clone()
	 */
	@Override
	public Validator clone() {
		return new ApplyRuleValidator();
	}

}
