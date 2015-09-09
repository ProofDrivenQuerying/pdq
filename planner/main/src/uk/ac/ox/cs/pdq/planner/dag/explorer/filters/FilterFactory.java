package uk.ac.ox.cs.pdq.planner.dag.explorer.filters;

import uk.ac.ox.cs.pdq.planner.PlannerParameters.FilterTypes;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;

/**
 * Creates filters based on the input arguments.

 * @author Efthymia Tsamoura
 *
 */
public class FilterFactory<S extends AccessibleChaseState> {

	private final FilterTypes type;


	/**
	 * Constructor for FilterFactory.
	 * @param type FilterTypes
	 */
	public FilterFactory(FilterTypes type) {
		this.type = type;
	}

	/**
	 * @return Filter
	 */
	public Filter getInstance() {
		if(this.type == null) {
			return null;
		}
		switch(this.type) {
		case FACT_DOMINATED_FILTER:
			return new FactDominationFilter();
		case NUMERICALLY_DOMINATED_FILTER:
			return new NumericalFactDominationFilter();
		default:
			throw new java.lang.IllegalArgumentException();
		}
	}
}
