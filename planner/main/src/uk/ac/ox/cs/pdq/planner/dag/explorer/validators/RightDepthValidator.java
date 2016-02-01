package uk.ac.ox.cs.pdq.planner.dag.explorer.validators;

import uk.ac.ox.cs.pdq.planner.dag.ConfigurationUtility;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;


/**
 * Requires the input pair of configurations to be non trivial and the right's depth to be <= the depth threshold
 * @author Efthymia Tsamoura
 *
 */
public class RightDepthValidator implements Validator{

	private final int depthThreshold;

	public RightDepthValidator() {
		this.depthThreshold = 3;
	}

	/**
	 * Constructor for RightDepthThrottlingValidator.
	 * @param depthThreshold int
	 */
	public RightDepthValidator(int depthThreshold) {
		this.depthThreshold = depthThreshold;
	}

	/**
	 * @param left DAGConfiguration
	 * @param right DAGConfiguration
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.dag.explorer.validators.Validator#validate(DAGConfiguration, DAGConfiguration)
	 */
	@Override
	public boolean validate(DAGChaseConfiguration left, DAGChaseConfiguration right) {
		return right.getHeight() <= this.depthThreshold && ConfigurationUtility.isNonTrivial(left, right);
	}

	/**
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
	 * @return Validator
	 * @see uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator#clone()
	 */
	@Override
	public Validator clone() {
		return new RightDepthValidator(this.depthThreshold);
	}

}
