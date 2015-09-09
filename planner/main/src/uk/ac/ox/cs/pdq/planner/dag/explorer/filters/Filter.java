package uk.ac.ox.cs.pdq.planner.dag.explorer.filters;

import java.util.Collection;

import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;

/**
 * Filters out configurations
 *
 * @author Efthymia Tsamoura
 */
public interface Filter {

	/**
	 *
	 * @param configurations
	 * @return the configurations that are filtered out
	 */
	Collection<DAGChaseConfiguration> filter(Collection<DAGChaseConfiguration> configurations);

}
