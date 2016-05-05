package uk.ac.ox.cs.pdq.algebra.predicates;

import uk.ac.ox.cs.pdq.util.Tuple;

/**
 * TOCOMMENT This seems to be a superclass of conjunctions of equalities; hence the isSatisifed method. 
 * I don't like this design. Predicate is well-defined notion in Logic and DB theory. Maybe SelectionCondition would be better. 
 * 
 * Common interface to all relational operator predicates.
 *
 * @author Julien Leblay
 */
public interface Predicate {

	/**
	 * Checks if is satisfied.
	 *
	 * @param t the t
	 * @return true if the tuple t satisfies the predicate
	 */
	boolean isSatisfied(Tuple t);
}
