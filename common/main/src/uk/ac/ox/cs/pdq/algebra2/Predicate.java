package uk.ac.ox.cs.pdq.algebra2;

import java.util.Collection;


/**
 * TOCOMMENT This seems to be a superclass of conjunctions of equalities; hence the isSatisifed method. 
 * I don't like this design. Predicate is well-defined notion in Logic and DB theory. Maybe SelectionCondition would be better. 
 * 
 * Common interface to all relational operator predicates.
 *
 * @author Julien Leblay
 */
public interface Predicate {

	Collection<EqualityPredicate> getEqualityPredicates();
	
}
