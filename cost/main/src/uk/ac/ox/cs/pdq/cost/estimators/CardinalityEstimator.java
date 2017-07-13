package uk.ac.ox.cs.pdq.cost.estimators;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;

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
	void estimate(RelationalTerm logOp);

	/**
	 * Computes and updates the estimated cardinality of the given operator,
	 * and all its descendants, but only if this has not already been done.
	 *
	 * @param logOp the log op
	 */
	void estimateIfNeeded(RelationalTerm logOp);

	/**
	 * Clone.
	 *
	 * @return a copy of this cardinality estimator.
	 */
	CardinalityEstimator clone();
}
