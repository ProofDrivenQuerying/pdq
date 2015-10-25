package uk.ac.ox.cs.pdq.planner.cardinality;

import uk.ac.ox.cs.pdq.planner.PlannerParameters.CardinalityEstimatorTypes;

public class CardinalityEstimatorFactory {

	/**
	 * 
	 * @param type
	 * @param cardinalityEstimator
	 * @return
	 */
	public static CardinalityEstimator getInstance(CardinalityEstimatorTypes type) {
		switch(type) {
		case DEFAULT:
			break;
		default:
			break;
		}
		return null;
	}
}
