package uk.ac.ox.cs.pdq.planner.dag.explorer.validators;

import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.DAGConfiguration;


/**
 * Requires the input pair of configurations right's depth to be <= the depth threshold.
 *
 * @author Efthymia Tsamoura
 */
public class RightDepthPairValidator implements PairValidator{
	/** The depth threshold. */
	private final int depthThreshold;

	/**
	 * Instantiates a new right depth validator.
	 */
	public RightDepthPairValidator() {
		this.depthThreshold = 3;
	}

	/**
	 * Constructor for RightDepthThrottlingValidator.
	 * @param depthThreshold int
	 */
	public RightDepthPairValidator(int depthThreshold) {
		this.depthThreshold = depthThreshold;
	}

	/**
	 * Validate.
	 *
	 * @param left DAGConfiguration
	 * @param right DAGConfiguration
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.PairValidator.explorer.validators.Validator#validate(DAGConfiguration, DAGConfiguration)
	 */
	@Override
	public boolean validate(DAGChaseConfiguration left, DAGChaseConfiguration right) {
		return right.getHeight() <= this.depthThreshold;
	}

	/**
	 * Validate.
	 *
	 * @param left DAGConfiguration
	 * @param right DAGConfiguration
	 * @param depth int
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.PairValidator.explorer.validators.Validator#validate(DAGConfiguration, DAGConfiguration, int)
	 */
	@Override
	public boolean validate(DAGChaseConfiguration left, DAGChaseConfiguration right, int depth) {
		return this.validate(left, right);
	}

	/**
	 * Clone.
	 *
	 * @return Validator
	 * @see uk.ac.ox.cs.pdq.planner.dag.explorer.validators.PairValidator#clone()
	 */
	@Override
	public PairValidator clone() {
		return new RightDepthPairValidator(this.depthThreshold);
	}

}
