// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.dag.explorer.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dominance.Dominance;

/**
 * Filters out the fact dominated configurations.
 *
 * @author Efthymia Tsamoura
 */
public class DominationFilter implements Filter {

	/** The fact dominance. */
	private final Dominance dominance;
	
	public DominationFilter(Dominance dominance) {
		Preconditions.checkNotNull(dominance);
		this.dominance = dominance;
	}

	/**
	 * Filter.
	 *
	 * @param configurations Collection<DAGConfiguration>
	 * @return Collection<DAGConfiguration>
	 * @see uk.ac.ox.cs.pdq.dag.explorer.filters.Filter#filter(Collection<DAGConfiguration>)
	 */
	@Override
	public Collection<DAGChaseConfiguration> filter(Collection<DAGChaseConfiguration> configurations) {
		List<DAGChaseConfiguration> input = new ArrayList<>(configurations);
		Collection<DAGChaseConfiguration> filtered = new LinkedHashSet<>();
		int i = 0;
		int j = 1;
		int lSize = input.size();

		while(i < lSize - 1) {
			j = i + 1;
			if(!filtered.contains(input.get(i))) {
				while(j < lSize) {
					if(!(input.get(i) instanceof ApplyRule) && this.dominance.isDominated(input.get(i), input.get(j))) {
						filtered.add(input.get(i));
						break;
					}
					if(!(input.get(j) instanceof ApplyRule) && this.dominance.isDominated(input.get(j), input.get(i))) {
						filtered.add(input.get(j));
					}
					++j;
				}
			}
			++i;
		}
		return filtered;
	}
}
