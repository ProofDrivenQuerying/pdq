package uk.ac.ox.cs.pdq.planner.dag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.accessible.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.accessible.AccessibleSchema.InferredAccessibleRelation;
import uk.ac.ox.cs.pdq.planner.dag.BinaryConfiguration.BinaryConfigurationTypes;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.dominance.Dominance;
import uk.ac.ox.cs.pdq.planner.dominance.FactDominance;
import uk.ac.ox.cs.pdq.planner.dominance.FastFactDominance;
import uk.ac.ox.cs.pdq.planner.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * Utility class
 *
 * @author Efthymia Tsamoura
 */
public class ConfigurationUtility {

	/** Performs fact dominance checks */
	private final static FactDominance factDominance = new FastFactDominance(false);
	
	/**
	 * @param configuration
	 * @return
	 * 		true if the input configuration is a left-deep one
	 */
	public static boolean isLeftDeep(DAGConfiguration configuration) {
		if(configuration instanceof BinaryConfiguration) {
			if (((BinaryConfiguration) configuration).getRight() instanceof BinaryConfiguration) {
				return false;
			}
			return isLeftDeep(((BinaryConfiguration) configuration).getLeft());
		}
		return true;
	}

	/**
	 * @param configuration
	 * @return
	 * 		the ApplyRule sub-configurations of the input configuration
	 */
	public static Collection<ApplyRule> getApplyRules(DAGConfiguration configuration) {
		Collection<ApplyRule> ret = new LinkedHashSet<>();
		if(configuration instanceof BinaryConfiguration) {
			ret.addAll(getApplyRules(((BinaryConfiguration) configuration).getLeft()));
			ret.addAll(getApplyRules(((BinaryConfiguration) configuration).getRight()));
		}
		else {
			ret.add((ApplyRule) configuration);
		}
		return ret;
	}

	/**
	 * @param configuration
	 * @return 
	 * 		the ApplyRule sub-configurations (ordered by appearance) of the input configuration
	 */
	public static List<ApplyRule> getApplyRulesList(DAGConfiguration configuration) {
		List<ApplyRule> ret = new ArrayList<>();
		if(configuration instanceof BinaryConfiguration) {
			ret.addAll(getApplyRules(((BinaryConfiguration) configuration).getLeft()));
			ret.addAll(getApplyRules(((BinaryConfiguration) configuration).getRight()));
		}
		else {
			ret.add((ApplyRule) configuration);
		}
		return ret;
	}

	/**
	 * @param configuration
	 * @return
	 * 		the subconfigurations of the input configuration
	 */
	public static Collection<DAGConfiguration> getSubconfigurations(DAGConfiguration configuration) {
		Collection<DAGConfiguration> ret = new LinkedHashSet<>();
		if(configuration instanceof BinaryConfiguration) {
			ret.add(configuration);
			ret.addAll(getSubconfigurations(((BinaryConfiguration) configuration).getLeft()));
			ret.addAll(getSubconfigurations(((BinaryConfiguration) configuration).getRight()));
		}
		else {
			ret.add(configuration);
		}
		return ret;
	}

	/**
	 * @param left
	 * @param right
	 * @return true if the input pair of configurations is composable
	 */
	public static Boolean isComposable(DAGChaseConfiguration left, DAGChaseConfiguration right) {
		if(!right.getInput().isEmpty() && left.getOutput().containsAll(right.getInput())) {
			return true;
		}
		return false;
	}

	/**
	 * @param left
	 * @param right
	 * @return true if the input pair of configurations is output independent
	 */
	public static Boolean isOutputIndependent(DAGChaseConfiguration left, DAGChaseConfiguration right) {
		return Collections.disjoint(left.getProperOutput(), right.getProperOutput());
	}

	/**
	 * @param left
	 * @param right
	 * @return true if the input pair of configurations is mergeable
	 */
	public static Boolean isMergeable(DAGChaseConfiguration left, DAGChaseConfiguration right) {
		if (Collections.disjoint(left.getProperOutput(), right.getInput())
				&& Collections.disjoint(left.getInput(), right.getProperOutput())) {
			return true;
		}
		return false;
	}

	/**
	 * @param left
	 * @param right
	 * @return
	 * 		true if the input pair of configurations is non trivial.
	 * 		An ordered pair of configurations (left, right)
			is non-trivial if the output facts of the right configuration are not included in
			the output facts of left configuration and vice versa, and if the ApplyRule
			subconfigurations of left and right do not overlap.
	 */
	public static Boolean isNonTrivial(DAGChaseConfiguration left, DAGChaseConfiguration right) {
		if (left.equals(right)){
			return false;
		}

		// Julien: Commented out -> not part of the definition of non-trivial
		Collection<ApplyRule> leftApplyRules = left.getApplyRules();
		Collection<ApplyRule> rightApplyRules = right.getApplyRules();
		for (ApplyRule leftApplyRule:leftApplyRules) {
			for (ApplyRule rightApplyRule:rightApplyRules) {
				if (leftApplyRule.getFacts().equals(rightApplyRule.getFacts())) {
					return false;
				}
			}
		}

		if (left.getOutputFacts().containsAll(right.getOutputFacts())
				|| right.getOutputFacts().containsAll(left.getOutputFacts())) {
			return false;
		}
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
	 * @param left
	 * @param right
	 * @return a merged state
	 */
	public static AccessibleChaseState merge(DAGChaseConfiguration left, DAGChaseConfiguration right) {
		return left.getState().merge(right.getState());
	}
	
	/**
	 * @param left
	 * @param right
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
	 * @param left
	 * @param right
	 * @return the output constants of the binary configuration composed from the left and right input configurations
	 */
	public static List<Constant> getOutput(DAGChaseConfiguration left, DAGChaseConfiguration right) {
		List<Constant> output = Lists.newArrayList(left.getOutput());
		output.addAll(right.getOutput());
		return output;
	}

	/**
	 *
	 * @param left
	 * @param right
	 * @return the bushiness of the binary configuration composed from the left and right input configurations
	 */
	public static Integer getBushiness(DAGChaseConfiguration left, DAGChaseConfiguration right) {
		int bushiness = left.getBushiness() + right.getBushiness();
		if(right instanceof BinaryConfiguration) {
			bushiness++;
		}
		return bushiness;
	}

	/**
	 * @param left
	 * @param right
	 * @return the type of the binary configuration composed from the left and right input configurations
	 */
	public static BinaryConfigurationTypes getCombinationType(DAGChaseConfiguration left, DAGChaseConfiguration right) {
		if (isNonTrivial(left, right)) {
			if (isComposable(left, right)) {
				if (isOutputIndependent(left, right)) {
					return BinaryConfigurationTypes.PCOMPOSE;
				}
				return BinaryConfigurationTypes.JCOMPOSE;
			} else if (isMergeable(left, right)) {
				return BinaryConfigurationTypes.MERGE;
			}
			else {
				return BinaryConfigurationTypes.GENCOMPOSE;
			}
		}
		return null;
	}

	/**
	 * @param input
	 * @return a deep copy of the input array of dominance objects
	 */
	public static Dominance[] arrayCopy(Dominance[] input) {
		Dominance[] array = new Dominance[input.length];
		for(int i = 0; i < input.length; ++i) {
			array[i] = input[i].clone();
		}
		return array;
	}

	/**
	 * @param left
	 * @param right
	 * @param validators
	 * @param depth
	 * @return
	 * 		true if the binary configuration composed from the left and right input configurations passes the validation tests,
	 * 		i.e., satisfies given shape restrictions.
	 * 		If depth > 0, then the corresponding binary configuration must be of the given depth.
	 */
	public static boolean validate(DAGChaseConfiguration left, DAGChaseConfiguration right, List<Validator> validators, int depth) {
		if(depth > 0) {
			for(int i = 0; i < validators.size(); ++i) {
				if(!validators.get(i).validate(left, right, depth)) {
					return false;
				}
			}
			return true;
		}
		for(int i = 0; i < validators.size(); ++i) {
			if(!validators.get(i).validate(left, right)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param left
	 * @param right
	 * @param validators
	 * @return
	 * 		true if the binary configuration composed from the left and right input configurations passes the validation tests,
	 * 		i.e., satisfies given shape restrictions.
	 */
	public static boolean validate(DAGChaseConfiguration left, DAGChaseConfiguration right, List<Validator> validators) {
		return validate(left, right, validators, -1);
	}

	/**
	 * @param configuration
	 * @param bestPlan Best plan found so far
	 * @param successDominance Success dominance checks
	 * @return true if the input configuration is not success dominated by the best plan
	 */
	public static Boolean getPotential(DAGChaseConfiguration configuration, DAGPlan bestPlan, SuccessDominance successDominance) {
		return bestPlan == null || !successDominance.isDominated(configuration.getPlan(), bestPlan);
	}
	
	/**
	 * @param target 
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
	 * @param left
	 * @param right
	 * @param bestPlan Best plan
	 * @param costEstimator Estimates a plan's cost
	 * @param successDominance Success dominance checks
	 * @return true if the configuration composed from the left and right input configurations is not success dominated by the best plan
	 */
	public static Boolean getPotential(DAGChaseConfiguration left, 
			DAGChaseConfiguration right,
			DAGPlan bestPlan, 
			CostEstimator<DAGPlan> costEstimator, 
			SuccessDominance successDominance) {
		BinaryConfiguration.BinaryConfigurationTypes type = getCombinationType(left, right);
		if(type != null) {
			if(bestPlan == null) {
				return true;
			}
			DAGPlan plan = PlanGenerator.toPlan(left, right, type);
			costEstimator.cost(plan);
			return !successDominance.isDominated(plan, bestPlan);
		}
		return false;
	}
	
	/**
	 * @param constants
	 * @return
	 * 		the output facts of the input configuration that are sufficient to make each input constant accessible
	 */
	public static Collection<Set<Predicate>> getMinimalSetThatExposesConstants(DAGChaseConfiguration configuration, Collection<Constant> constants, AccessibleSchema accessibleSchema) {
		Collection<Set<Predicate>> ret = new LinkedHashSet<>();
		Collection<ApplyRule> applyRules = configuration.getApplyRules();
		//Create all combinations of the constituting ApplyRule configurations
		Set<Set<ApplyRule>> sets = Sets.powerSet(Sets.newLinkedHashSet(applyRules));

		//For each combination of ApplyRule configurations
		for(Set<ApplyRule> set:sets) {
			//The set of facts (that came from the ApplyRule configurations of this iteration) that
			//are sufficient to make each input constant accessible
			Set<Predicate> minimalSet = new LinkedHashSet<>();
			//The output constants of the minimalSet of facts
			Set<Constant> observed = new LinkedHashSet<>();
			//The constants that are still inaccessible
			Set<Constant> remaining = Sets.newLinkedHashSet(constants);

			for(ApplyRule applyRule:set) {

				Relation baseRelation = applyRule.getRelation();
				InferredAccessibleRelation infAccRelation = accessibleSchema.getInferredAccessibleRelation(baseRelation);

				Collection<Predicate> facts = applyRule.getFacts();
				if(observed.containsAll(applyRule.getInput())) {
					/*
					 * If the input constants of the current fact are all provided by previously added facts
					 * and makes at least one of the remaining constants accessible
					 * then add it to the minimal set
					 */
					for(Predicate fact:facts) {
						Set<Constant> properOutput = Utility.getConstants(fact);
						properOutput.removeAll(applyRule.getInput());
						if(!Sets.intersection(remaining, properOutput).isEmpty()) {
							remaining.removeAll(properOutput);
							observed.addAll(properOutput);
							minimalSet.add(new Predicate(infAccRelation, fact.getTerms()));
						}
					}
				}

				if(remaining.isEmpty()) {
					ret.add(minimalSet);
				}
			}
		}
		return ret;
	}

}
