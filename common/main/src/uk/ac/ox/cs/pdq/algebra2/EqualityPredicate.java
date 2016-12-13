package uk.ac.ox.cs.pdq.algebra2;


/**
 * Common interface to equality predicates.
 *
 * @author Julien Leblay
 */
public interface EqualityPredicate extends Predicate {
	
	/**
	 * TOCOMMENT this seems way to much abstraction. Equality predicates always have one positions to compare for
	 * equality against something: either another positions (in AttributeEqualityPredicate), or a constant (in ConstantEqualityPredicate)
	 * Gets the position.
	 *
	 * @return int
	 */
	int getPosition();
}
