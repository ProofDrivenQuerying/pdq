package uk.ac.ox.cs.pdq.cost.estimators;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;

// TODO: Auto-generated Javadoc
/**
 * Interface for all context-specific implementations of cardinality estimators.
 *
 * @author Julien Leblay
 */
public interface CardinalityEstimator {
	
	/**
	 * Computes and updates the estimated cardinality of the given operator,
	 * and all its descendants.
	 *
	 * @param logOp the log op
	 */
	void estimateCardinality(RelationalTerm logOp);
	
	RelationalTermCardinalityMetadata getCardinalityMetadata(RelationalTerm o);

	/**
	 * Clone.
	 *
	 * @return a copy of this cardinality estimator.
	 */
	CardinalityEstimator clone();
}
