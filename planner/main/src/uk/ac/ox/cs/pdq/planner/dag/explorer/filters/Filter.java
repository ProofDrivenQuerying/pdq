package uk.ac.ox.cs.pdq.planner.dag.explorer.filters;

import java.util.Collection;

import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;

// TODO: Auto-generated Javadoc
/**
 * Filters out configurations.
 *
 * @author Efthymia Tsamoura
 */
public interface Filter {

	/**
	 * Filter.
	 *
	 * @param configurations the configurations
	 * @return the configurations that are filtered out
	 */
	Collection<DAGChaseConfiguration> filter(Collection<DAGChaseConfiguration> configurations);

}
