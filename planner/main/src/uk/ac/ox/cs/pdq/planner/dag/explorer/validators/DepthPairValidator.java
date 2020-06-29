// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.dag.explorer.validators;

import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.DAGConfiguration;

/**
 * Requires the input pair of configurations combined depth to be <= the depth threshold.
 *
 * @author Efthymia Tsamoura
 */
public class DepthPairValidator implements PairValidator{

	/** The depth threshold. */
	private final int depthThreshold;

	/**
	 * Constructor for DepthThrottlingValidator.
	 * @param depthThreshold int
	 */
	public DepthPairValidator(int depthThreshold) {
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
		return left.getHeight() <= this.depthThreshold;
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
		return left.getHeight() + right.getHeight() == depth && this.validate(left, right);
	}

	/**
	 * Clone.
	 *
	 * @return Validator
	 * @see uk.ac.ox.cs.pdq.planner.dag.explorer.validators.PairValidator#clone()
	 */
	@Override
	public PairValidator clone() {
		return new DepthPairValidator(this.depthThreshold);
	}

}
