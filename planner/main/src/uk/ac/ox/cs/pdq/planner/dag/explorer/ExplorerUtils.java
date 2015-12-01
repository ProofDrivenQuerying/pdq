package uk.ac.ox.cs.pdq.planner.dag.explorer;

import java.util.Collection;

import uk.ac.ox.cs.pdq.planner.dag.ConfigurationUtility;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dominance.Dominance;

/**
 * Utility class
 * @author Efthymia Tsamoura
 *
 */
public class ExplorerUtils {

	/**
	 *
	 * @param targets
	 * @param source
	 * @return
	 * 		the configuration that dominates the input
	 */
	public static DAGChaseConfiguration isDominated(Dominance[] dominance, Collection<DAGChaseConfiguration> targets, DAGChaseConfiguration source) {
		for(DAGChaseConfiguration target:targets) {
			if(ConfigurationUtility.isDominatedBy(dominance, target, source)) {
				return target;
			}
		}
		return null;
	}

}
