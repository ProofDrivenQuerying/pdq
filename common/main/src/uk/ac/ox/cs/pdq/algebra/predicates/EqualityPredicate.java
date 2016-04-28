package uk.ac.ox.cs.pdq.algebra.predicates;


/**
 * Common interface to equality predicates.
 *
 * @author Julien Leblay
 */
public interface EqualityPredicate extends Predicate {
	
	/**
	 * Gets the position.
	 *
	 * @return int
	 */
	int getPosition();
}
