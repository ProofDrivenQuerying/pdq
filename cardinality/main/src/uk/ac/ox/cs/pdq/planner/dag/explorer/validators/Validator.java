/*
 * 
 */
package uk.ac.ox.cs.pdq.planner.dag.explorer.validators;

import uk.ac.ox.cs.pdq.planner.dag.DAGAnnotatedPlan;



// TODO: Auto-generated Javadoc
/**
 * Validates pairs of configurations to be composed, for example,
 * it checks whether or not the binary configuration composed from the given pair satisfies given shape restrictions.
 *
 * @author Efthymia Tsamoura
 */
public interface Validator {
	
	/**
	 * Validate.
	 *
	 * @param left the left
	 * @param right the right
	 * @return 		true if the considered configurations pass the validation test
	 */
	boolean validate(DAGAnnotatedPlan left, DAGAnnotatedPlan right);

	/**
	 * Validate.
	 *
	 * @param left the left
	 * @param right the right
	 * @param depth the depth
	 * @return true if the considered configurations pass the validation test, and their combined depth equals the given one
	 */
	boolean validate(DAGAnnotatedPlan left, DAGAnnotatedPlan right, int depth);

	/**
	 * Clone.
	 *
	 * @return Validator
	 */
	Validator clone();
}
