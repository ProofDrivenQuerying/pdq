package uk.ac.ox.cs.pdq.datasources.memory;

import java.util.Collection;

import uk.ac.ox.cs.pdq.util.Tuple;

/**
 * TOCOMMENT shouldn't this be connected to other Relation objects?
 * Common interface to all in-memory relations (table, views, etc.).
 * 
 * @author Julien Leblay
 */
public interface InMemoryRelation {

//	/**
//	 * Gets the name of the relation.
//	 *
//	 * @return the name of the relation
//	 */
//	String getName();

	/**
	 * Load tuples into the relation. Any existing tuples in the relation will 
	 * remain if not previously explicitly cleared.
	 * @param t Collection<Tuple>
	 */
	void load(Collection<Tuple> t);

	/**
	 * Clear tuples from the relation. 
	 */
	void clear();
}
