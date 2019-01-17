package uk.ac.ox.cs.pdq.planner.dag.explorer.validators;

import java.util.List;

import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;


/**
 * Utility class.
 *
 * @author Efthymia Tsamoura, Gabor
 */
public class DagChaseConfigurationValidation {

	/**
	 *
	 * @param left the left
	 * @param right the right
	 * @param validators the validators
	 * @param depth the depth
	 * @return 		true if the binary configuration composed from the left and right input configurations passes the validation tests,
	 * 		i.e., satisfies given shape restrictions.
	 * 		If depth > 0, then the corresponding binary configuration must be of the given depth.
	 */
	public static boolean validate(DAGChaseConfiguration left, DAGChaseConfiguration right, List<PairValidator> validators, int depth) {
		if(depth > 0) {
			for(int i = 0; i < validators.size(); ++i) {
				if(!validators.get(i).validate(left, right, depth)) 
					return false;
			}
			return true;
		}
		for(int i = 0; i < validators.size(); ++i) {
			if(!validators.get(i).validate(left, right)) 
				return false;
		}
		return true;
	}
	
}
