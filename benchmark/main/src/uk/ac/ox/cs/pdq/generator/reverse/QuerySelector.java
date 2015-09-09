package uk.ac.ox.cs.pdq.generator.reverse;

import uk.ac.ox.cs.pdq.fol.Query;


/**
 * Functional interface for accepting or rejected query based on arbirtary 
 * criteria.
 * 
 * @author Julien Leblay
 *
 * @param <Q>
 */
public interface QuerySelector {

	/**
	 * @param q
	 * @return true, if q satisfied some arbitrary criterion.
	 */
	public boolean accept(Query<?> q);
}
