package uk.ac.ox.cs.pdq.cost.estimators;

import uk.ac.ox.cs.pdq.algebra.RelationalOperator;

// TODO: Auto-generated Javadoc
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
	 * @param logOp the log op
	 */
	void estimate(RelationalOperator logOp);

	/**
	 * Computes and updates the estimated cardinality of the given operator,
	 * and all its descendants, but only if this has not already been done.
	 *
	 * @param logOp the log op
	 */
	void estimateIfNeeded(RelationalOperator logOp);

	/**
	 * Clone.
	 *
	 * @return a copy of this cardinality estimator.
	 */
	CardinalityEstimator clone();
}
