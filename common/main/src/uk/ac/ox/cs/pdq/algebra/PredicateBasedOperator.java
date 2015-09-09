package uk.ac.ox.cs.pdq.algebra;

import uk.ac.ox.cs.pdq.algebra.predicates.Predicate;

/**
 * Common interface to logical operator that applied some predicate to tuples.
 *
 * @author Julien Leblay
 */
public interface PredicateBasedOperator {
	/**
	 * @return Predicate
	 */
	Predicate getPredicate();
}
