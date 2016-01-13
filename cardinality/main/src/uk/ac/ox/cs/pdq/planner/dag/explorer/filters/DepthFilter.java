package uk.ac.ox.cs.pdq.planner.dag.explorer.filters;

import java.util.Collection;
import java.util.LinkedHashSet;

import uk.ac.ox.cs.pdq.planner.dag.DAGAnnotatedPlan;

/**
 * Filters out the configurations with depth > the depth threshold
 * @author Efthymia Tsamoura
 *
 * @param 
 */
public class DepthFilter implements Filter {

	/** Depth threshold. Configurations with depth > this threshold will be filtered out*/
	private final int depthThreshold;

	public DepthFilter() {
		this.depthThreshold = 3;
	}

	/**
	 * Constructor for DepthFilter.
	 * @param depthThreshold int
	 */
	public DepthFilter(int depthThreshold) {
		this.depthThreshold = depthThreshold;
	}

	/**
	 * @param configurations Collection<DAGConfiguration>
	 * @return Collection<DAGConfiguration>
	 * @see uk.ac.ox.cs.pdq.dag.explorer.filters.Filter#filter(Collection<DAGConfiguration>)
	 */
	@Override
	public Collection<DAGAnnotatedPlan> filter(Collection<DAGAnnotatedPlan> configurations) {
		Collection<DAGAnnotatedPlan> filtered = new LinkedHashSet<>();
		for(DAGAnnotatedPlan configuration: configurations) {
			if(configuration.getHeight() >= this.depthThreshold) {
				filtered.add(configuration);
			}
		}
		return filtered;
	}

}
