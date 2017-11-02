package uk.ac.ox.cs.pdq.data.sql;

import uk.ac.ox.cs.pdq.data.PhysicalQuery;
import uk.ac.ox.cs.pdq.fol.Formula;

/**
 * This is a query that was created from formulas such as ConjunctiveQuery or
 * a dependency activeness check, but converted to SQL language.
 * 
 * @author Gabor
 *
 */
public class SQLUpdate extends PhysicalQuery {
	
	public SQLUpdate(Formula formula) {
		super(formula);
	}

	/** Main function of this class, converts the ConjunctiveQuery or other formula into an executable sql query string.
	 * @return
	 */
	public String toSQLQueryString() {
		return null;
	}
}
