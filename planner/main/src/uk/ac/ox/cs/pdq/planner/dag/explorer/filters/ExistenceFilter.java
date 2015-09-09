package uk.ac.ox.cs.pdq.planner.dag.explorer.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;

import com.google.common.base.Preconditions;

/**
 * Filters out the fact dominated configurations
 * @author Efthymia Tsamoura

 * @param 
 */
public class ExistenceFilter<S extends AccessibleChaseState> implements Filter {

	private final List<Pair<Relation,AccessMethod>> accesses;

	public ExistenceFilter(List<Pair<Relation,AccessMethod>> accesses) {
		Preconditions.checkArgument(accesses != null && !accesses.isEmpty());
		this.accesses = accesses;
	}
	
	/**
	 * @param configurations Collection<DAGConfiguration>
	 * @return Collection<DAGConfiguration>
	 * @see uk.ac.ox.cs.pdq.dag.explorer.filters.Filter#filter(Collection<DAGConfiguration>)
	 */
	@Override
	public Collection<DAGChaseConfiguration> filter(Collection<DAGChaseConfiguration> configurations) {
		Collection<DAGChaseConfiguration> filtered = new LinkedHashSet<>();
		for(DAGChaseConfiguration configuration:configurations) {
			List<Pair<Relation,AccessMethod>> accesses = new ArrayList<>();
			for(ApplyRule applyRule:configuration.getApplyRulesList()) {
				accesses.add(Pair.of(applyRule.getRelation(), applyRule.getRule().getAccessMethod()));
			}
			if(Collections.indexOfSubList(this.accesses, accesses) == -1) {
				filtered.add(configuration);
			}
		}
		return filtered;
	}
}
