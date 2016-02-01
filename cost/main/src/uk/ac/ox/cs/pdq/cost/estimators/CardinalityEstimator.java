package uk.ac.ox.cs.pdq.cost.estimators;

import uk.ac.ox.cs.pdq.algebra.RelationalOperator;

/**
 * Interface for all context-specific implementations of cardinality estimators.
 *
 * @author Julien Leblay
 */
public interface CardinalityEstimator extends Cloneable {
	/**
	 * Computes and updates the estimated cardinality of the given operator,
	 * and all its descendants.
	 *
	 * @param logOp
	 */
	void estimate(RelationalOperator logOp);

	/**
	 * Computes and updates the estimated cardinality of the given operator,
	 * and all its descendants, but only if this has not already been done.
	 *
	 * @param logOp
	 */
	void estimateIfNeeded(RelationalOperator logOp);

	/**
	 * @return a copy of this cardinality estimator.
	 */
	CardinalityEstimator clone();
}
