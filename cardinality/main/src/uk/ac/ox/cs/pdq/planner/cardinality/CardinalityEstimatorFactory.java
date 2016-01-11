package uk.ac.ox.cs.pdq.planner.cardinality;

import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.CardinalityEstimatorTypes;

public class CardinalityEstimatorFactory {

	/**
	 * 
	 * @param type
	 * @param cardinalityEstimator
	 * @return
	 */
	public static CardinalityEstimator getInstance(CardinalityEstimatorTypes type, Catalog catalog) {
		switch(type) {
		case DEFAULT:
			return new DefaultCardinalityEstimator(catalog);
		default:
			break;
		}
		return null;
	}
}
