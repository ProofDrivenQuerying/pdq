package uk.ac.ox.cs.pdq.data.sql;

import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.ox.cs.pdq.data.PhysicalQuery;
import uk.ac.ox.cs.pdq.db.sql.FromCondition;
import uk.ac.ox.cs.pdq.db.sql.SelectCondition;
import uk.ac.ox.cs.pdq.db.sql.WhereCondition;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * This is a query that was created from formulas such as ConjunctiveQuery or a
 * dependency activeness check, but converted to SQL language.
 * 
 * @author Gabor
 *
 */
public class SQLQuery extends PhysicalQuery {
	private SelectCondition projections;
	private String SqlQueryString;
	private WhereCondition where;

	/**
	 * @param source
	 */
	protected SQLQuery(ConjunctiveQuery source) {
		super(source);
	}

	/**
	 * Main function of this class, converts the ConjunctiveQuery or other formula
	 * into an executable sql query string.
	 * 
	 * @return
	 */
	public String toSQLQueryString() {
		return null;
	}

	public SelectCondition getProjections() {
		return projections;
	}

	public void setProjections(SelectCondition projections) {
		this.projections = projections;
	}
	public static SQLQuery createSQLQuery(ConjunctiveQuery source, SqlDatabaseInstance instance) {
		String query = "";
		FromCondition from = instance.createFromStatement(source.getAtoms());
		SelectCondition projections = instance.createProjections(source.getAtoms());
		WhereCondition where = new WhereCondition();
		WhereCondition equalities = instance.createAttributeEqualities(source.getAtoms(),instance.schema);
		WhereCondition constantEqualities = instance.createEqualitiesWithConstants(source.getAtoms(),instance.schema);
		where.addCondition(equalities);
		where.addCondition(constantEqualities);
		query = instance.buildSQLQuery(projections, from, where);
		SQLQuery sqlQuery = new SQLQuery(source);
		sqlQuery.setProjections(projections);
		sqlQuery.setSqlQueryString(query);
		return sqlQuery;
	}
	public static SQLQuery createQueryDifference(ConjunctiveQuery leftQuery, ConjunctiveQuery rightQuery, SqlDatabaseInstance databaseInstance) {
		return null;
	}

	public String convertToSqlQueryString(String databaseName) {
		if (SqlQueryString==null) {
			SqlQueryString = "Select * from " + databaseName + "." + this.formula.getAtoms()[0].getPredicate().getName();
		}
		return SqlQueryString;
	}

	public void setSqlQueryString(String sqlQueryString) {
		SqlQueryString = sqlQueryString;
	}

	public WhereCondition getWhere() {
		return where;
	}

	public void setWhere(WhereCondition where) {
		this.where = where;
	}

	public Map<String, Variable> getProjectedVariables() {
		return new LinkedHashMap<>();
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.data.PhysicalQuery#toString()
	 */
	@Override
	public String toString() {
		return "SQLQuery - " + super.toString() + " / SQL: " + SqlQueryString;
	}

}
