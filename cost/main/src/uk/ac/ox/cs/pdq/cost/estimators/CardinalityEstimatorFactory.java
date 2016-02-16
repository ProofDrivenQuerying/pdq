package uk.ac.ox.cs.pdq.cost.estimators;


import uk.ac.ox.cs.pdq.cost.CostParameters.CardinalityEstimationTypes;
import uk.ac.ox.cs.pdq.cost.CostParameters.CostTypes;
import uk.ac.ox.cs.pdq.db.Schema;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * A factory of relation cardinality estimators.
 *
 * @author Julien Leblay
 */
public class CardinalityEstimatorFactory {

	/**
	 * Gets the single instance of CardinalityEstimatorFactory.
	 *
	 * @param costType the cost type
	 * @param cardType the card type
	 * @param schema the schema
	 * @return a cardinality estimator implementation corresponding to that specified
	 * in the initialConfig.
	 */
	public static CardinalityEstimator getInstance(CostTypes costType, CardinalityEstimationTypes cardType, Schema schema) {
		Preconditions.checkArgument(costType != null, "Cardinatlity estimation type param must is not defined.");
		switch (cardType) {
		case NAIVE:
			return new NaiveCardinalityEstimator(schema);
		default:
			throw new IllegalArgumentException("Cardinality estimation " + cardType + "  not yet supported.");
		}
	}
}
