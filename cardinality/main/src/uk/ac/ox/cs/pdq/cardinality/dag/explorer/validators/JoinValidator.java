/*
 * 
 */
package uk.ac.ox.cs.pdq.cardinality.dag.explorer.validators;

import org.apache.commons.collections4.CollectionUtils;

import uk.ac.ox.cs.pdq.cardinality.dag.ConfigurationUtility;
import uk.ac.ox.cs.pdq.cardinality.dag.DAGAnnotatedPlan;
import uk.ac.ox.cs.pdq.cardinality.dag.DAGConfiguration;


// TODO: Auto-generated Javadoc
/**
 * Requires the input pair of configurations to be non trivial
 * and at least one of the input configurations to be an UnaryAnnotatedPlan.
 *
 * @author Efthymia Tsamoura
 */
public class JoinValidator implements Validator{

	/**
	 * Instantiates a new join validator.
	 */
	public JoinValidator() {
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
		return !CollectionUtils.intersection(left.getOutput(), right.getOutput()).isEmpty() 
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
	 * @see uk.ac.ox.cs.pdq.cardinality.dag.explorer.validators.Validator#clone()
	 */
	@Override
	public Validator clone() {
		return new JoinValidator();
	}

}
