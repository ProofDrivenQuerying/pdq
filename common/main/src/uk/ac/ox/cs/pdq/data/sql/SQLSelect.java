package uk.ac.ox.cs.pdq.data.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;

import uk.ac.ox.cs.pdq.algebra.AttributeEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.SimpleCondition;
import uk.ac.ox.cs.pdq.data.ConjunctiveQueryDescriptor;
import uk.ac.ox.cs.pdq.data.PhysicalQuery;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * This is a query that was created from formulas such as ConjunctiveQuery or a
 * dependency activeness check, but converted to SQL language.
 * 
 * @author Gabor
 *
 */
public class SQLSelect extends PhysicalQuery {
	protected String sqlQueryString;
	protected List<String> whereConditions = new ArrayList<>();
	protected List<String> fromTableName = new ArrayList<>();
	protected String select;
	
	public SQLSelect(ConjunctiveQuery source, SqlDatabaseInstance instance) {
		super(source, instance.schema);
		sqlQueryString = init(source, instance);
	}

	private String init(ConjunctiveQuery source, SqlDatabaseInstance instance) {
		String dbNameDot = instance.databaseParameters.getDatabaseName() + "."; 
		String query = "SELECT ";
		boolean first = true;
		
		// SELECT free variables			
		for (ConjunctiveQueryDescriptor a: this.getQueryAtoms()) {
			for (Variable v: a.getFreeVariableToPosition().keySet()) {
				if (!first)
					query += " , ";
				first = false;
				query += dbNameDot+ a.getRelation().getName() + "." + a.getAttributeAtIndex(a.getFreeVariableToPosition().get(v)).getName();
			}
		}
		select = query;
		// FROM table names			
		query += " FROM " + getTableNamesPartOfQuery(this.getQueryAtoms(), instance.databaseParameters.getDatabaseName()) + " ";
		// WHERE CONSTANT EQUALITY CONDITIONS
		boolean whereAdded = false;
		first = true;
		for (ConjunctiveQueryDescriptor a: this.getQueryAtoms()) {
			if (a.hasConstantEqualityCondition()) {
				if (!whereAdded) {
					query += " WHERE ";
					whereAdded = true;
				}
				if (!first) {
					query += " AND ";
				}
				first = false;
				for (Attribute attribute : a.getConstantEqualityConditions().keySet()) {
					query += " " + dbNameDot+a.getRelation().getName() + "." + attribute.getName() + " = " + SqlDatabaseInstance.convertTermToSQLString(attribute, a.getConstantEqualityConditions().get(attribute)) + " ";
				}
			}
		}
		
		// WHERE ATTRIBUTE EQUALITY CONDITIONS
		for (Conjunction conjunction: getAttributeEqualityConditions().keySet()) {
			ConjunctiveCondition attributeEqualities = getAttributeEqualityConditions().get(conjunction);
			if (attributeEqualities==null || attributeEqualities.getSimpleConditions().length == 0)
				continue;
			for (SimpleCondition condition:attributeEqualities.getSimpleConditions()) {
				if (!whereAdded) {
					query += " WHERE ";
					whereAdded = true;
				}
				if (!first) {
					query += " AND ";
				}
				first = false;
				AttributeEqualityCondition aec = (AttributeEqualityCondition)condition;
				ConjunctiveQueryDescriptor atomLeft = ConjunctiveQueryDescriptor.findAtomFor(queryAtoms, conjunction.getAtoms()[0].getPredicate().getName());
				int shift = 0;
				int atomIndex =1;
				while (aec.getOther()>=shift + conjunction.getAtoms()[atomIndex].getTerms().length) {
					shift += conjunction.getAtoms()[atomIndex].getTerms().length;
					atomIndex++;
				}
				String rightRelationName = conjunction.getAtoms()[atomIndex].getPredicate().getName();
				String rightAttributeName = ConjunctiveQueryDescriptor.findAtomFor(queryAtoms,rightRelationName).getAttributeAtIndex(aec.getOther()-shift).getName();
				String newCondition = dbNameDot + atomLeft.getRelation().getName() + "." + atomLeft.getAttributeAtIndex(aec.getPosition()).getName() + " = " + 
						dbNameDot + rightRelationName + "." + rightAttributeName;
				whereConditions.add(newCondition);
				query += newCondition;
			}
		}
		
		return query;		
	}
	
	private String getTableNamesPartOfQuery(Collection<ConjunctiveQueryDescriptor> atoms, String databaseName) {
		for (ConjunctiveQueryDescriptor a:atoms) {
			fromTableName.add(databaseName + "." + a.getRelation().getName());
		}
		return Joiner.on(",").join(fromTableName); 
	}

	public String getSqlQueryString() {
		return sqlQueryString;
	}

	public void setSqlQueryString(String sqlQueryString) {
		this.sqlQueryString = sqlQueryString;
	}

	public Map<String, Variable> getProjectedVariables() {
		return new LinkedHashMap<>();
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
		return "SQLSelect - " + super.toString() + " / SQL: " + sqlQueryString;
	}

	@Override
	protected PhysicalQuery getRightQuery() {
		return null;
	}

}
