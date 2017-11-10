package uk.ac.ox.cs.pdq.data.sql;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;

import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;

/**
 * This is a query that was created from formulas such as ConjunctiveQuery or a
 * dependency activeness check, but converted to SQL language.
 * 
 * @author Gabor
 *
 */
public class SQLNestedSelect extends SQLSelect {

	/**
	 * This will be used in case this query was created by the difference of two
	 * conjunctive query.
	 */
	protected SQLSelect rightQuery;

	public SQLNestedSelect(ConjunctiveQuery left, SQLSelect right, SqlDatabaseInstance instance) {
		super(left, instance);
		this.rightQuery = right;
		
		List<String> differentConditions = new ArrayList<String>(right.whereConditions);
		differentConditions.removeAll(this.whereConditions);
		List<String> differentTableNames = new ArrayList<String>(right.fromTableName);
		differentTableNames.removeAll(this.fromTableName);
		
		if (whereConditions.isEmpty()) {
			sqlQueryString += " WHERE \n ";
		} else {
			sqlQueryString += " AND \n ";
		}
		sqlQueryString += " NOT EXISTS ( SELECT " + Joiner.on(",").join(right.select) + " FROM " + Joiner.on(",").join(differentTableNames);
		if (differentConditions.isEmpty()) {
			sqlQueryString += ")";
		} else {
			sqlQueryString += " WHERE " + Joiner.on(" AND ").join(differentConditions) + " )";
		}
	}
	
	public String getSqlQueryString() {
		return sqlQueryString;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ox.cs.pdq.data.PhysicalQuery#toString()
	 */
	@Override
	public String toString() {
		return "SQLNestedSelect - " + super.toString() + " Nesting : " + rightQuery;
	}

	public SQLSelect getRightQuery() {
		return rightQuery;
	}
}
