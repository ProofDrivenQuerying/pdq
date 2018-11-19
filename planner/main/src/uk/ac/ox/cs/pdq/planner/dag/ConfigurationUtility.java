package uk.ac.ox.cs.pdq.planner.dag;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.PairValidator;
import uk.ac.ox.cs.pdq.planner.dominance.Dominance;


/**
 * Utility class.
 *
 * @author Efthymia Tsamoura
 */
public class ConfigurationUtility {

	/**
	 * Gets the apply rule configurations that lie within a given configuration.
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
	 * 
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
	public static boolean validate(DAGChaseConfiguration left, DAGChaseConfiguration right, List<PairValidator> validators, int depth) {
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
	 *
	 * @param dominance the detector of dominance
	 * @param target the target
	 * @param source the source
	 * @return true if the source configuration is dominated by the target one
	 */
	public static boolean isDominatedBy(Dominance[] dominance, DAGChaseConfiguration target, DAGChaseConfiguration source) {
		for(Dominance detector:dominance) {
			if(detector.isDominated(source, target)) {
				return true;
			}
		}
		return false;
	}

}
