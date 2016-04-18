package uk.ac.ox.cs.pdq.generator.reverse;

import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Query;

// TODO: Auto-generated Javadoc
/**
 * Functional interface for accepting or rejected query based on arbitrary 
 * criteria.
 *
 * @author Julien Leblay
 */
public interface QuerySelector {

	/**
	 * Checks if the given query passes the query-selection test.
	 *
	 * @param q the q
	 * @return true, iff the given query passes the query-selection test.
	 */
	public boolean accept(ConjunctiveQuery q);
}
