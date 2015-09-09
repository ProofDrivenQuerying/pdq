package uk.ac.ox.cs.pdq.planner.dag.explorer;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.dag.BinaryConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.ConfigurationUtility;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.DAGConfiguration;

/**
 * Utility class
 * @author Efthymia Tsamoura
 *
 */
public class ExplorerUtils {

	/**
	 *
	 * @param configurations
	 * @param input
	 * @return
	 * 		the configuration that dominates the input
	 */
	public static DAGChaseConfiguration isDominated(Collection<DAGChaseConfiguration> configurations, DAGChaseConfiguration input) {
		for(DAGChaseConfiguration configuration:configurations) {
			if(input.isDominatedBy(configuration)) {
				return configuration;
			}
		}
		return null;
	}

	/**
	 * @param input 
	 * @return the binary subconfigurations of the input configuration
	 */
	public static Set<BinaryConfiguration> getBinarySubconfigurations(Set<DAGChaseConfiguration> input) {
		Set<BinaryConfiguration> subconfigurations = new LinkedHashSet<>();
		for(DAGChaseConfiguration configuration:input) {
			for(DAGConfiguration subconfiguration:configuration.getSubconfigurations()) {
				if(subconfiguration instanceof BinaryConfiguration) {
					subconfigurations.add((BinaryConfiguration) subconfiguration);
				}
			}
		}
		return subconfigurations;
	}

	/**
	 * @param left DAGChaseConfiguration
	 * @param right DAGChaseConfiguration
	 * @param bestPlan P
	 * @return true if the configuration composed by the input configurations has the potential to lead to the minimum cost plan
	 */
	public static Boolean getPotential(DAGChaseConfiguration left, DAGChaseConfiguration right, DAGPlan bestPlan) {
		return ConfigurationUtility.getPotential(left, right, bestPlan, left.getCostEstimator(), left.getSuccessDominanceDetector());
	}

	/**
	 * @param configuration DAGChaseConfiguration
	 * @param bestPlan P
	 * @return true if the input configuration has the potential to lead to the minimum cost plan
	 */
	public static Boolean getPotential(DAGChaseConfiguration configuration, DAGPlan bestPlan) {
		return bestPlan == null
				|| !configuration.getSuccessDominanceDetector().isDominated(configuration.getPlan(), bestPlan);
	}

}
