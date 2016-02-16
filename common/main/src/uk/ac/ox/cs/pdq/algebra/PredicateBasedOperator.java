package uk.ac.ox.cs.pdq.algebra;

import uk.ac.ox.cs.pdq.algebra.predicates.Predicate;

// TODO: Auto-generated Javadoc
/**
 * Common interface to logical operator that applied some predicate to tuples.
 *
 * @author Julien Leblay
 */
public interface PredicateBasedOperator {
	
	/**
	 * Gets the predicate.
	 *
	 * @return Predicate
	 */
	Predicate getPredicate();
}
