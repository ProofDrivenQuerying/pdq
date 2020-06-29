// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.dag.explorer.filters;

import java.util.Collection;

import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;

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
