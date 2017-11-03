package uk.ac.ox.cs.pdq.data.memory;

import java.util.Map;

import uk.ac.ox.cs.pdq.data.PhysicalDatabaseInstance;
import uk.ac.ox.cs.pdq.data.PhysicalQuery;
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
public class MemoryQuery extends PhysicalQuery {
	private SelectCondition projections;
	private String SqlQueryString;
	private WhereCondition where;

	public MemoryQuery(Formula formula) {
		super(formula);
	}

	public MemoryQuery(ConjunctiveQuery source, Map<Variable, Constant> finalProjectionMapping, PhysicalDatabaseInstance instance) {
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
	public MemoryQuery(String string) {
		super(null);
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

	public String getSqlQueryString() {
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
}
