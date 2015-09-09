package uk.ac.ox.cs.pdq.planner.dag.explorer.validators;

import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;


/**
 * Validates pairs of configurations to be composed, for example,
 * it checks whether or not the binary configuration composed from the given pair satisfies given shape restrictions.
 * @author Efthymia Tsamoura
 *
 * @param 
 */
public interface Validator {
	/**
	 *
	 * @param left
	 * @param right
	 * @return
	 * 		true if the considered configurations pass the validation test
	 */
	boolean validate(DAGChaseConfiguration left, DAGChaseConfiguration right);

	/**
	 *
	 * @param left
	 * @param right
	 * @param depth
	 * @return true if the considered configurations pass the validation test, and their combined depth equals the given one
	 */
	boolean validate(DAGChaseConfiguration left, DAGChaseConfiguration right, int depth);

	/**
	 * @return Validator
	 */
	Validator clone();
}
