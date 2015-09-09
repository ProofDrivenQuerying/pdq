package uk.ac.ox.cs.pdq.algebra.predicates;

import java.util.Collection;

import uk.ac.ox.cs.pdq.util.Tuple;

/**
 * Common interface to all relational operator predicates.
 *
 * @author Julien Leblay
 */
public interface Predicate {

	/**
	 * @param t
	 * @return true if the tuple t satisfies the predicate
	 */
	boolean isSatisfied(Tuple t);
	
	/**
	 * @param predicate the (possibly nested) predicate to flatten, if null the empty collection is returned.
	 * @return a collection of predicate remove the nesting of conjunction that
	 * it may contain.
	 */
	public Collection<Predicate> flatten();
}
