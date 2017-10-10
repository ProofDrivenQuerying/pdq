package uk.ac.ox.cs.pdq.planner.dag.explorer.filters;

import uk.ac.ox.cs.pdq.planner.PlannerParameters.FilterTypes;
import uk.ac.ox.cs.pdq.planner.dominance.FastFactDominance;
import uk.ac.ox.cs.pdq.planner.dominance.NumericalFactDominance;

/**
 * Creates filters based on the input arguments.

 * @author Efthymia Tsamoura
 *
 */
public class FilterFactory {

	/** The type. */
	private final FilterTypes type;


	/**
	 * Constructor for FilterFactory.
	 * @param type FilterTypes
	 */
	public FilterFactory(FilterTypes type) {
		this.type = type;
	}

	/**
	 * Gets the single instance of FilterFactory.
	 *
	 * @return Filter
	 */
	public Filter getInstance() {
		if(this.type == null)
			return null;
		switch(this.type) {
		case FACT_DOMINATED_FILTER:
			return new DominationFilter(new FastFactDominance(false));
		case NUMERICALLY_DOMINATED_FILTER:
			return new DominationFilter(new NumericalFactDominance());
		default:
			throw new java.lang.IllegalArgumentException();
		}
	}
}
