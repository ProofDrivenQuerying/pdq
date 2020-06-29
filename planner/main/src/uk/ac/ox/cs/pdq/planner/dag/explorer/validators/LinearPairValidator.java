// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.dag.explorer.validators;

import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.DAGConfiguration;


/**
 * Requires the input pair of configurations composition to be a closed left-deep configuration.
 *
 * @author Efthymia Tsamoura
 */
public class LinearPairValidator implements PairValidator{
	/**
	 * Instantiates a new linear validator.
	 */
	public LinearPairValidator() {
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
		return  right instanceof ApplyRule
				&& left.isClosed()
				&& left.getOutput().containsAll(right.getInput());
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
		return new LinearPairValidator();
	}
	
}
