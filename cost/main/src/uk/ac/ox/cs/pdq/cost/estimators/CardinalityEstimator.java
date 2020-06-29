// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.cost.estimators;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;

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
	 * 
	 *
	 * @return a copy of this cardinality estimator.
	 */
	CardinalityEstimator clone();
}
