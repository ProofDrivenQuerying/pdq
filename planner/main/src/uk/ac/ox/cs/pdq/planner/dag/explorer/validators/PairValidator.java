package uk.ac.ox.cs.pdq.planner.dag.explorer.validators;

import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;


/**
 * Checks whether the binary configuration Binary(c,c') composed from a given pair of configurations c, c' satisfies given shape restrictions or not.
 * @author Efthymia Tsamoura
 *
 */
public interface PairValidator {
	
	/**
	 * Validate.
	 *
	 * @param left the left
	 * @param right the right
	 * @return 		true if the considered configurations pass the validation test
	 */
	boolean validate(DAGChaseConfiguration left, DAGChaseConfiguration right);

	/**
	 * Validate.
	 *
	 * @param left the left
	 * @param right the right
	 * @param depth the depth
	 * @return true if the considered configurations pass the validation test, and their combined depth equals the given one
	 */
	boolean validate(DAGChaseConfiguration left, DAGChaseConfiguration right, int depth);

	/**
	 * Clone.
	 *
	 * @return Validator
	 */
	PairValidator clone();
}
