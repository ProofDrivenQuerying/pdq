/*
 * 
 */
package uk.ac.ox.cs.pdq.cardinality.reasoning.chase.dominance;

import java.util.ArrayList;

import uk.ac.ox.cs.pdq.cardinality.CardinalityParameters.DominanceTypes;
import uk.ac.ox.cs.pdq.cardinality.CardinalityParameters.SuccessDominanceTypes;
import uk.ac.ox.cs.pdq.cardinality.estimator.CardinalityEstimator;

// TODO: Auto-generated Javadoc
/**
 * Creates cost dominance detectors using the input parameters.
 * The available options are:
 * 		CLOSED for closed dominance and
 * 		OPEN for open dominance (an open configuration may dominate a closed one)
 *
 * @author Efthymia Tsamoura
 */
public class DominanceFactory {

	/**
	 * Gets the single instance of DominanceFactory.
	 *
	 * @param type the type
	 * @param cardinalityEstimator the cardinality estimator
	 * @return single instance of DominanceFactory
	 */
	public static Dominance[] getInstance(DominanceTypes type, CardinalityEstimator cardinalityEstimator) {
		ArrayList<Dominance> detector = new ArrayList<>();
		switch(type) {
		case TIGHT:
			detector.add(new TightQualityDominance(cardinalityEstimator));
			break;
		default:
			break;
		}
		Dominance<?>[] array = new Dominance[detector.size()];
		detector.toArray(array);
		return array;
	}
	
	/**
	 * Gets the single instance of DominanceFactory.
	 *
	 * @param type the type
	 * @param cardinalityEstimator the cardinality estimator
	 * @return single instance of DominanceFactory
	 */
	public static Dominance getInstance(SuccessDominanceTypes type, CardinalityEstimator cardinalityEstimator) {
		switch(type) {
		case LOOSE:
			return new LooseQualityDominance(cardinalityEstimator);
		default:
			break;
		}
		return null;
	}
}
