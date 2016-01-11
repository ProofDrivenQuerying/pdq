package uk.ac.ox.cs.pdq.planner.dag.explorer.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dominance.NumericalFactDominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState;


/**
 * Filters out the numerically fact dominated configurations
 * @author Efthymia Tsamoura
 *
 * @param 
 */
public class NumericalFactDominationFilter<S extends AccessibleChaseState> implements Filter {

	private final NumericalFactDominance factDominance = new NumericalFactDominance();

	/**
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
					if(!(input.get(i) instanceof ApplyRule) && this.factDominance.isDominated(input.get(i), input.get(j))) {
						filtered.add(input.get(i));
						break;
					}
					if(!(input.get(j) instanceof ApplyRule) && this.factDominance.isDominated(input.get(j), input.get(i))) {
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
