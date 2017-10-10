package uk.ac.ox.cs.pdq.planner.dag.explorer.validators;

import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.ConfigurationUtility;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.DAGConfiguration;

/**
 * Requires the input pair of configurations to be non trivial, their combined depth to be <= the depth threshold
 * and at least one of the input configurations to be an ApplyRule.
 *
 * @author Efthymia Tsamoura
 */
public class ApplyRuleDepthValidator implements Validator{

	/** The depth threshold. */
	private final int depthThreshold;

	/**
	 * Instantiates a new apply rule depth validator.
	 */
	public ApplyRuleDepthValidator() {
		this.depthThreshold = 3;
	}

	/**
	 * Constructor for FactDepthThrottlingValidator.
	 * @param depthThreshold int
	 */
	public ApplyRuleDepthValidator(int depthThreshold) {
		this.depthThreshold = depthThreshold;
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
		return (left instanceof ApplyRule || right instanceof ApplyRule)
				&& left.getHeight() <= this.depthThreshold
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
		return new ApplyRuleDepthValidator(this.depthThreshold);
	}

}
