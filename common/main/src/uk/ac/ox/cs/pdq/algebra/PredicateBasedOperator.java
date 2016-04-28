package uk.ac.ox.cs.pdq.algebra;

import uk.ac.ox.cs.pdq.algebra.predicates.Predicate;

/**
 * ??? I'm not sure what predicate means here or in the join class
 * Common interface to a logical operator that applies a predicate to tuples.
 *
 * @author Julien Leblay
 */
public interface PredicateBasedOperator {
	
	/**
	 * Gets the predicate.
	 *
	 * @return Atom
	 */
	Predicate getPredicate();
}
