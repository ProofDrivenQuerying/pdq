package uk.ac.ox.cs.pdq.data.memory;

import uk.ac.ox.cs.pdq.data.PhysicalQuery;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;

/**
 * This is a query that was created from formulas such as ConjunctiveQuery or a
 * dependency activeness check, but converted to SQL language.
 * 
 * @author Gabor
 *
 */
public class MemoryQuery extends PhysicalQuery {

	public MemoryQuery(ConjunctiveQuery source) {
		super(source);
	}
}
