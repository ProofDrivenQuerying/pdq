package uk.ac.ox.cs.pdq.data.memory;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;

/**
 * This is a query that was created from formulas such as ConjunctiveQuery or a
 * dependency activeness check, but converted to SQL language.
 * 
 * @author Gabor
 *
 */
public class MemoryNestedQuery extends MemoryQuery {

	private MemoryQuery rightQuery;

	/**
	 * This query will represent the difference between two Queries.
	 * 
	 * @param leftQuery
	 * @param rightQuery
	 */
	public MemoryNestedQuery(ConjunctiveQuery leftQuery, MemoryQuery rightQuery, Schema schema) {
		super(leftQuery, schema);
		this.rightQuery = rightQuery;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ox.cs.pdq.data.PhysicalQuery#getFormula()
	 */
	@Override
	protected ConjunctiveQuery getConjunctiveQuery() {
		return super.getConjunctiveQuery();
	}

	protected MemoryQuery getRightQuery() {
		return rightQuery;
	}
}
