package uk.ac.ox.cs.pdq.algebra.predicates;

import uk.ac.ox.cs.pdq.util.Tuple;

// TODO: Auto-generated Javadoc
/**
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
