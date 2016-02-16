package uk.ac.ox.cs.pdq.planner.dag.explorer.validators;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.planner.dag.ConfigurationUtility;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;

// TODO: Auto-generated Javadoc
/**
 * Requires the input pair of configurations to be non trivial.
 * An ordered pair of configurations (left, right) is non-trivial 
 * if the output facts of the right configuration are not included in the output facts of left configuration and vice versa.
 * @author Efthymia Tsamoura
 *
 */
public class DefaultValidator implements Validator{

	/**
	 * Instantiates a new default validator.
	 */
	public DefaultValidator() {
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
	public boolean validate(DAGChaseConfiguration left, DAGChaseConfiguration right) {
		return ConfigurationUtility.isNonTrivial(left, right);
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
	public boolean validate(DAGChaseConfiguration left, DAGChaseConfiguration right, int depth) {
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
		return new DefaultValidator();
	}

	/**
	 * Deep copy.
	 *
	 * @param input the input
	 * @return a deep copy of the input validators list
	 */
	public static List<Validator> deepCopy(List<Validator> input) {
		List<Validator> list = new ArrayList<>();
		for(int i = 0; i < input.size(); ++i) {
			list.add(input.get(i).clone());
		}
		return list;
	}
}
