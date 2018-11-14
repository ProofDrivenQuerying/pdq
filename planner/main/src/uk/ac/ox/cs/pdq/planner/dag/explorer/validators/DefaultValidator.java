package uk.ac.ox.cs.pdq.planner.dag.explorer.validators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.DAGConfiguration;
import uk.ac.ox.cs.pdq.planner.dominance.FactDominance;
import uk.ac.ox.cs.pdq.planner.dominance.FastFactDominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;

/**
 * Requires the input pair of configurations to be non trivial.
 * An ordered pair of configurations (left, right) is non-trivial 
 * if the output facts of the right configuration are not included in the output facts of left configuration and vice versa.
 * @author Efthymia Tsamoura
 *
 */
public class DefaultValidator implements Validator{
	/**  Performs fact dominance checks. */
	public final static FactDominance factDominance = new FastFactDominance(false);

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
		return DefaultValidator.isNonTrivial(left, right);
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
		return this.validate(left, right);
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
	 * TOCOMMENT: THE COMMENT AND THE CODE DO NOT MATCH, SO SOMETHING HAS TO CHANGE
	 *
	 * @param left the left
	 * @param right the right
	 * @return 		true if the input pair of configurations is non trivial.
	 * 		An ordered pair of configurations (left, right)
	 * 			is non-trivial if the output facts of the right configuration are not included in
	 * 			the output facts of left configuration and vice versa, and if the ApplyRule
	 * 			subconfigurations of left and right do not overlap.
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
	public static List<Validator> deepCopy(List<Validator> input) {
		List<Validator> list = new ArrayList<>();
		for(int i = 0; i < input.size(); ++i) {
			list.add(input.get(i).clone());
		}
		return list;
	}
}
