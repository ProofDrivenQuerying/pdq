package uk.ac.ox.cs.pdq.data.sql;

import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.ox.cs.pdq.data.PhysicalDatabaseInstance;
import uk.ac.ox.cs.pdq.data.PhysicalQuery;
import uk.ac.ox.cs.pdq.db.sql.FromCondition;
import uk.ac.ox.cs.pdq.db.sql.SelectCondition;
import uk.ac.ox.cs.pdq.db.sql.WhereCondition;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Formula;
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

	/** The formula must be conjunctiveQuery or Dependency
	 * @param formula
	 */
	public SQLQuery(Formula formula) {
		super(formula);
	}

	public SQLQuery(ConjunctiveQuery source, Map<Variable, Constant> finalProjectionMapping, PhysicalDatabaseInstance instance) {
		super(source);
		// SQLStatementBuilder stb =
		// canonicalDatabaseInstance.getDatabaseConnection().getSQLStatementBuilder();
		// FromCondition from = stb.createFromStatement(source.getAtoms());
		// projections =
		// stb.createProjections(source.getAtoms(),canonicalDatabaseInstance.getDatabaseConnection());
		// where = new WhereCondition();
		// WhereCondition equalities =
		// stb.createAttributeEqualities(source.getAtoms(),canonicalDatabaseInstance.getDatabaseConnection().getSchema());
		// WhereCondition constantEqualities =
		// stb.createEqualitiesWithConstants(source.getAtoms(),canonicalDatabaseInstance.getDatabaseConnection().getSchema());
		// WhereCondition equalitiesWithProjectedVars =
		// stb.createEqualitiesRespectingInputMapping(source.getAtoms(),
		// finalProjectionMapping,canonicalDatabaseInstance.getDatabaseConnection().getSchema());
		//
		// WhereCondition factproperties = null;
		// if (facts != null && !facts.isEmpty())
		// factproperties = stb.enforceStateMembership(source.getAtoms(),
		// canonicalDatabaseInstance.getDatabaseConnection().getRelationNamesToDatabaseTables(),this.facts);
		// else
		// factproperties = new WhereCondition();
		//
		// where.addCondition(equalities);
		// where.addCondition(constantEqualities);
		// where.addCondition(equalitiesWithProjectedVars);
		// where.addCondition(factproperties);
		//
		// SqlQueryString = stb.buildSQLQuery(projections, from, where);

	}

	/**
	 * @param string
	 */
	public SQLQuery(Formula formula, String string) {
		super(formula);
		SqlQueryString = string;
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
	public static SQLQuery createSQLQuery(ConjunctiveQuery source, Map<Variable, Constant> finalProjectionMapping, SqlDatabaseInstance instance) {
		String query = "";
		FromCondition from = instance.createFromStatement(source.getAtoms());
		SelectCondition projections = instance.createProjections(source.getAtoms());
		WhereCondition where = new WhereCondition();
		WhereCondition equalities = instance.createAttributeEqualities(source.getAtoms(),instance.schema);
		WhereCondition constantEqualities = instance.createEqualitiesWithConstants(source.getAtoms(),instance.schema);
//		WhereCondition equalitiesWithProjectedVars = instance.createEqualitiesRespectingInputMapping(source.getAtoms(), finalProjectionMapping,instance.schema);

		where.addCondition(equalities);
		where.addCondition(constantEqualities);
	//	where.addCondition(equalitiesWithProjectedVars);

		query = instance.buildSQLQuery(projections, from, where);
		SQLQuery sqlQuery = new SQLQuery(source);
		sqlQuery.setProjections(projections);
		sqlQuery.setSqlQueryString(query);
		return sqlQuery;
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
