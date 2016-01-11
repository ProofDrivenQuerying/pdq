package uk.ac.ox.cs.pdq.planner.dag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.Dominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.FactDominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.FastFactDominance;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState;

import com.google.common.collect.Lists;


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
		if(configuration instanceof BinaryAnnotatedPlan) {
			if (((BinaryAnnotatedPlan) configuration).getRight() instanceof BinaryAnnotatedPlan) {
				return false;
			}
			return isLeftDeep(((BinaryAnnotatedPlan) configuration).getLeft());
		}
		return true;
	}
	
	/**
	 * @param configuration
	 * @return
	 * 		the UnaryAnnotatedPlan sub-configurations of the input configuration
	 */
	public static Collection<UnaryAnnotatedPlan> getUnaryAnnotatedPlans(DAGConfiguration configuration) {
		Collection<UnaryAnnotatedPlan> ret = new LinkedHashSet<>();
		if(configuration instanceof BinaryAnnotatedPlan) {
			ret.addAll(getUnaryAnnotatedPlans(((BinaryAnnotatedPlan) configuration).getLeft()));
			ret.addAll(getUnaryAnnotatedPlans(((BinaryAnnotatedPlan) configuration).getRight()));
		}
		else {
			ret.add((UnaryAnnotatedPlan) configuration);
		}
		return ret;
	}

	/**
	 * @param configuration
	 * @return 
	 * 		the UnaryAnnotatedPlan sub-configurations (ordered by appearance) of the input configuration
	 */
	public static List<UnaryAnnotatedPlan> getUnaryAnnotatedPlansList(DAGConfiguration configuration) {
		List<UnaryAnnotatedPlan> ret = new ArrayList<>();
		if(configuration instanceof BinaryAnnotatedPlan) {
			ret.addAll(getUnaryAnnotatedPlansList(((BinaryAnnotatedPlan) configuration).getLeft()));
			ret.addAll(getUnaryAnnotatedPlansList(((BinaryAnnotatedPlan) configuration).getRight()));
		}
		else {
			ret.add((UnaryAnnotatedPlan) configuration);
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
		if(configuration instanceof BinaryAnnotatedPlan) {
			ret.add(configuration);
			ret.addAll(getSubconfigurations(((BinaryAnnotatedPlan) configuration).getLeft()));
			ret.addAll(getSubconfigurations(((BinaryAnnotatedPlan) configuration).getRight()));
		}
		else {
			ret.add(configuration);
		}
		return ret;
	}

	/**
	 * @param left
	 * @param right
	 * @return
	 * 		true if the input pair of configurations is non trivial.
	 * 		An ordered pair of configurations (left, right)
			is non-trivial if the output facts of the right configuration are not included in
			the output facts of left configuration and vice versa, and if the UnaryAnnotatedPlan
			subconfigurations of left and right do not overlap.
	 */
	public static Boolean isNonTrivial(DAGAnnotatedPlan left, DAGAnnotatedPlan right) {
		if (left.equals(right)){
			return false;
		}
		Collection<UnaryAnnotatedPlan> leftApplyRules = left.getUnaryAnnotatedPlans();
		Collection<UnaryAnnotatedPlan> rightApplyRules = right.getUnaryAnnotatedPlans();
		return CollectionUtils.intersection(leftApplyRules, rightApplyRules).isEmpty();
	}

	/**
	 * @param left
	 * @param right
	 * @return a merged state
	 */
	public static ChaseState merge(DAGAnnotatedPlan left, DAGAnnotatedPlan right) {
		return left.getState().merge(right.getState());
	}
	
	/**
	 * @param left
	 * @param right
	 * @return the output constants of the binary configuration composed from the left and right input configurations
	 */
	public static List<Constant> getOutput(DAGAnnotatedPlan left, DAGAnnotatedPlan right) {
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
	public static Integer getBushiness(DAGAnnotatedPlan left, DAGAnnotatedPlan right) {
		int bushiness = left.getBushiness() + right.getBushiness();
		if(right instanceof BinaryAnnotatedPlan) {
			bushiness++;
		}
		return bushiness;
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
	public static boolean validate(DAGAnnotatedPlan left, DAGAnnotatedPlan right, List<Validator> validators, int depth) {
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
	 * @param target 
	 * @return true if this configuration is dominated by the input one
	 */
	public static boolean isDominatedBy(Dominance[] dominance, DAGAnnotatedPlan target, DAGAnnotatedPlan source) {
		for(Dominance detector:dominance) {
			if(detector.isDominated(source, target)) {
				return true;
			}
		}
		return false;
	}

//	/**
//	 * @param left
//	 * @param right
//	 * @param best Best plan
//	 * @param costEstimator Estimates a plan's cost
//	 * @param dominance Success dominance checks
//	 * @return true if the configuration composed from the left and right input configurations is not success dominated by the best plan
//	 */
//	public static Boolean getPotential(DAGAnnotatedPlan left, 
//			DAGAnnotatedPlan right,
//			DAGAnnotatedPlan best, 
//			Chaser egd, 
//			HomomorphismDetector detector,
//			Collection<? extends Constraint> dependencies,
//			CardinalityEstimator cardinalityEstimator) {
//		if(isNonTrivial(left, right)) {
//			if(best == null) {
//				return true;
//			}
//			double adjustedQuality = cardinalityEstimator.adjustedQualityOf(left, right);
//			Pair<Integer,Double> size = cardinalityEstimator.sizeQualityOf(left, right, egd, detector, dependencies);
//			if(size.getRight() >= best.getSize() && adjustedQuality <= best.getAdjustedQuality()) {
//				return false;
//			}
//			return true;
//		}
//		return false;
//	}

}
