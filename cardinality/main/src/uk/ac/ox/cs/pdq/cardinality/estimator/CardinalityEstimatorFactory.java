/*
 * 
 */
package uk.ac.ox.cs.pdq.cardinality.estimator;

import uk.ac.ox.cs.pdq.cardinality.CardinalityParameters.CardinalityEstimatorTypes;
import uk.ac.ox.cs.pdq.cost.statistics.Catalog;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating CardinalityEstimator objects.
 */
public class CardinalityEstimatorFactory {

	/**
	 * Gets the single instance of CardinalityEstimatorFactory.
	 *
	 * @param type the type
	 * @param catalog the catalog
	 * @return single instance of CardinalityEstimatorFactory
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
