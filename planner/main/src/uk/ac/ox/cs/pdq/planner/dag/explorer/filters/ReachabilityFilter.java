package uk.ac.ox.cs.pdq.planner.dag.explorer.filters;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.apache.commons.collections4.CollectionUtils;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;
import uk.ac.ox.cs.pdq.util.Utility;


/**
 * Filters out the non-reachable configurations
 * @author Efthymia Tsamoura
 *
 * @param 
 */
public class ReachabilityFilter<S extends AccessibleChaseState> implements Filter {

	private final Query<?> query;

	/**
	 * Constructor for ReachabilityFilter.
	 * @param query Query<?>
	 */
	public ReachabilityFilter(Query<?> query) {
		this.query = query;
	}

	/**
	 * @param configurations Collection<DAGConfiguration>
	 * @return Collection<DAGConfiguration>
	 * @see uk.ac.ox.cs.pdq.dag.explorer.filters.Filter#filter(Collection<DAGConfiguration>)
	 */
	@Override
	public Collection<DAGChaseConfiguration> filter(Collection<DAGChaseConfiguration> configurations) {
		Collection<DAGChaseConfiguration> filtered = new LinkedHashSet<>();
		Collection<Constant> canonical = Utility.getConstants(this.query.getCanonical().getPredicates());
		for(DAGChaseConfiguration configuration: configurations) {
			if(!CollectionUtils.containsAny(configuration.getOutput(), canonical)) {
				filtered.add(configuration);
			}
		}
		return filtered;
	}


}
