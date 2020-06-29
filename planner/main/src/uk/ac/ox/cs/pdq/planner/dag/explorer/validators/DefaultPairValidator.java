// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.dag.explorer.validators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.DAGConfiguration;
import uk.ac.ox.cs.pdq.planner.dominance.FactDominance;
import uk.ac.ox.cs.pdq.planner.dominance.FastFactDominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.ChaseConfiguration;

/**
 * Requires the input pair of configurations to be non trivial.
 * An ordered pair of configurations (left, right) is non-trivial 
 * if the output facts of the right configuration are not included in the output facts of left configuration and vice versa.
 * @author Efthymia Tsamoura
 *
 */
public class DefaultPairValidator implements PairValidator{
	/**  Performs fact dominance checks. */
	public final static FactDominance factDominance = new FastFactDominance(false);

	/**
	 * Instantiates a new default validator.
	 */
	public DefaultPairValidator() {
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
		return DefaultPairValidator.isNonTrivial(left, right);
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
		return new DefaultPairValidator();
	}

	/**
	 * @param left the left
	 * @param right the right
	 * @return 		true if the input pair of configurations is non trivial.
	 * 		An ordered pair of configurations (left, right)
	 * 			is non-trivial if the output facts of the right configuration are not included in
	 * 			the output facts of left configuration and vice versa, and if the ApplyRule
	 * 			subconfigurations of left and right do not overlap, and the two configuration does not dominate each other.
	 */
	public static Boolean isNonTrivial(DAGChaseConfiguration left, DAGChaseConfiguration right) {
		if (left.equals(right))
			return false;
		
		
		Collection<ApplyRule> leftApplyRules = left.getApplyRules();
		Collection<ApplyRule> rightApplyRules = right.getApplyRules();
		for (ApplyRule leftApplyRule:leftApplyRules) {
			for (ApplyRule rightApplyRule:rightApplyRules) {
				if (leftApplyRule.getFacts().equals(rightApplyRule.getFacts())) 
					return false;
			}
		}
	
		if (left.getOutputFacts().containsAll(right.getOutputFacts()) || right.getOutputFacts().containsAll(left.getOutputFacts())) 
			return false;
		
		for (DAGConfiguration l:left.getSubconfigurations()) {
			for (DAGConfiguration r:right.getSubconfigurations()) {
				if (l.equals(r)
						// Julien: Consider deleting -> not part of the definition of non-trivial
						|| factDominance.isDominated((ChaseConfiguration)l, (ChaseConfiguration)r)
						|| factDominance.isDominated((ChaseConfiguration)r, (ChaseConfiguration)l)
						) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Deep copy.
	 *
	 * @param input the input
	 * @return a deep copy of the input validators list
	 */
	public static List<PairValidator> deepCopy(List<PairValidator> input) {
		List<PairValidator> list = new ArrayList<>();
		for(int i = 0; i < input.size(); ++i) {
			list.add(input.get(i).clone());
		}
		return list;
	}
}
