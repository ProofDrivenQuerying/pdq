package uk.ac.ox.cs.pdq.planner.dag.explorer;

import java.util.Collection;

import uk.ac.ox.cs.pdq.planner.dag.ConfigurationUtility;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dominance.Dominance;

/**
 * Utility class.
 *
 * @author Efthymia Tsamoura
 */
public class ExplorerUtils {

	/**
	 * Checks if is dominated.
	 *
	 * @param dominance 		A list of objects that perform dominance checks
	 * @param targets the targets
	 * @param source the source
	 * @return 		true if there is a target configuration that dominates the source
	 */
	public static Boolean isDominated(Dominance[] dominance, Collection<DAGChaseConfiguration> targets, DAGChaseConfiguration source) {
		for(DAGChaseConfiguration target:targets) {
			if(ConfigurationUtility.isDominatedBy(dominance, target, source)) {
				return true;
			}
		}
		return false;
	}

}
