package uk.ac.ox.cs.pdq.planner.dag;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.dominance.Dominance;
import uk.ac.ox.cs.pdq.planner.dominance.FactDominance;
import uk.ac.ox.cs.pdq.planner.dominance.FastFactDominance;
import uk.ac.ox.cs.pdq.planner.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.util.PlanCreationUtility;


/**
 * Utility class.
 *
 * @author Efthymia Tsamoura
 */
public class ConfigurationUtility {

	/**  Performs fact dominance checks. */
	private final static FactDominance factDominance = new FastFactDominance(false);
	
	/**
	 * Checks if is left deep.
	 *
	 * @param configuration the configuration
	 * @return 		true if the input configuration is a left-deep one
	 */
	public static boolean isLeftDeep(DAGConfiguration configuration) {
		if(configuration instanceof BinaryConfiguration) {
			if (((BinaryConfiguration) configuration).getRight() instanceof BinaryConfiguration) 
				return false;
			return isLeftDeep(((BinaryConfiguration) configuration).getLeft());
		}
		return true;
	}

	/**
	 * Gets the apply rules.
	 *
	 * @param configuration the configuration
	 * @return 		the ApplyRule sub-configurations of the input configuration
	 */
	public static Collection<ApplyRule> getApplyRules(DAGConfiguration configuration) {
		Collection<ApplyRule> ret = new LinkedHashSet<>();
		if(configuration instanceof BinaryConfiguration) {
			ret.addAll(getApplyRules(((BinaryConfiguration) configuration).getLeft()));
			ret.addAll(getApplyRules(((BinaryConfiguration) configuration).getRight()));
		}
		else 
			ret.add((ApplyRule) configuration);
		return ret;
	}

	/**
	 * TOCOMMENT: WHAT IS THIS
	 *
	 * @param configuration the configuration
	 * @return 		the subconfigurations of the input configuration
	 */
	public static Collection<DAGConfiguration> getSubconfigurations(DAGConfiguration configuration) {
		Collection<DAGConfiguration> ret = new LinkedHashSet<>();
		if(configuration instanceof BinaryConfiguration) {
			ret.add(configuration);
			ret.addAll(getSubconfigurations(((BinaryConfiguration) configuration).getLeft()));
			ret.addAll(getSubconfigurations(((BinaryConfiguration) configuration).getRight()));
		}
		else 
			ret.add(configuration);
		return ret;
	}

	/**
	 * TOCOMMENT: WHAT DOES IT MEAN
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
		
		// Julien: Commented out -> not part of the definition of non-trivial
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
	 *
	 * @param left the left
	 * @param right the right
	 * @return the input constants of the binary configuration composed from the left and right input configurations
	 */
	public static List<Constant> getInput(DAGChaseConfiguration left, DAGChaseConfiguration right) {
		List<Constant> input = Lists.newArrayList();
		input.addAll(left.getInput());
		List<Constant> in2 = Lists.newArrayList(right.getInput());
		in2.removeAll(left.getOutput());
		input.addAll(in2);
		return input;
	}
	
	/**
	 *
	 * @param left the left
	 * @param right the right
	 * @return the output constants of the binary configuration composed from the left and right input configurations
	 */
	public static List<Constant> getOutput(DAGChaseConfiguration left, DAGChaseConfiguration right) {
		List<Constant> output = Lists.newArrayList(left.getOutput());
		output.addAll(right.getOutput());
		return output;
	}
	
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
	public static boolean validate(DAGChaseConfiguration left, DAGChaseConfiguration right, List<Validator> validators, int depth) {
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

	/**
	 * Gets the potential.
	 *
	 * @param configuration the configuration
	 * @param bestPlan Best plan found so far
	 * @param successDominance Success dominance checks
	 * @return true if the input configuration is not success dominated by the best plan
	 */
	public static Boolean getPotential(DAGChaseConfiguration configuration, RelationalTerm bestPlan, Cost costOfBestPlan, SuccessDominance successDominance) {
		return bestPlan == null || !successDominance.isDominated(configuration.getPlan(), configuration.getCost(), bestPlan, costOfBestPlan);
	}
	
	/**
	 *
	 * @param dominance the dominance
	 * @param target the target
	 * @param source the source
	 * @return true if this configuration is dominated by the input one
	 */
	public static boolean isDominatedBy(Dominance[] dominance, DAGChaseConfiguration target, DAGChaseConfiguration source) {
		for(Dominance detector:dominance) {
			if(detector.isDominated(source, target)) {
				return true;
			}
		}
		return false;
	}

	/**
	 *
	 * @param left the left
	 * @param right the right
	 * @param bestPlan Best plan found so far
	 * @param costEstimator Estimates a plan's cost
	 * @param successDominance Success dominance checks
	 * @return true if the configuration composed from the left and right input configurations is not success dominated by the best plan
	 */
	public static Boolean getPotential(DAGChaseConfiguration left, 
			DAGChaseConfiguration right,
			RelationalTerm bestPlan, 
			Cost costOfBestPlan,
			CostEstimator costEstimator, 
			SuccessDominance successDominance) {
		if (isNonTrivial(left, right)) {
			if(bestPlan == null) 
				return true;
			RelationalTerm plan = PlanCreationUtility.createPlan(left.getPlan(), right.getPlan());
			Cost cost = costEstimator.cost(plan);
			return !successDominance.isDominated(plan, cost, bestPlan, costOfBestPlan);
		}
		return false;
	}

}
