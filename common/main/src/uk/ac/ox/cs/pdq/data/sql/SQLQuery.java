package uk.ac.ox.cs.pdq.data.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;

import uk.ac.ox.cs.pdq.algebra.AttributeEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.SimpleCondition;
import uk.ac.ox.cs.pdq.data.PhysicalQuery;
import uk.ac.ox.cs.pdq.data.QueryAtom;
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
public class SQLQuery extends PhysicalQuery {
	private String sqlQueryString;

	public SQLQuery(ConjunctiveQuery source, SqlDatabaseInstance instance) {
		super(source, instance.schema);
		init(source, instance);
	}

	public SQLQuery(ConjunctiveQuery left, SQLQuery right, SqlDatabaseInstance instance) {
		super(left, right, instance.schema);
		init(left, instance);
	}
	
	private void init(ConjunctiveQuery source, SqlDatabaseInstance instance) {
		String dbNameDot = instance.databaseParameters.getDatabaseName() + "."; 
		String query = "SELECT ";
		boolean first = true;
		
		// SELECT free variables			
		for (QueryAtom a: this.getQueryAtoms()) {
			for (Variable v: a.getFreeVariableToPosition().keySet()) {
				if (!first)
					query += " , ";
				first = false;
				query += dbNameDot+ a.getRelation().getName() + "." + a.getAttributeAtIndex(a.getFreeVariableToPosition().get(v)).getName();
			}
		}
		
		// FROM table names			
		query += " FROM " + getTableNamesPartOfQuery(this.getQueryAtoms(), instance.databaseParameters.getDatabaseName()) + " ";
		// WHERE CONSTANT EQUALITY CONDITIONS
		boolean whereAdded = false;
		first = true;
		for (QueryAtom a: this.getQueryAtoms()) {
			if (a.hasConstantEqualityCondition()) {
				if (!whereAdded) {
					query += " WHERE ";
					whereAdded = true;
				}
				if (!first) {
					query += " AND ";
				}
				first = false;
				for (ConstantEqualityCondition cec : a.getConstantEqualityConditions()) {
					Attribute attribute = a.getAttributeAtIndex(cec.getPosition());
					query += " " + dbNameDot+a.getRelation().getName() + "." + attribute.getName() + " = " + SqlDatabaseInstance.convertTermToSQLString(attribute, cec.getConstant()) + " ";
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
				QueryAtom atomLeft = QueryAtom.findAtomFor(queryAtoms, conjunction.getAtoms()[0].getPredicate().getName());
				int shift = 0;
				int atomIndex =1;
				while (aec.getOther()>=shift + conjunction.getAtoms()[atomIndex].getTerms().length) {
					shift += conjunction.getAtoms()[atomIndex].getTerms().length;
					atomIndex++;
				}
				String rightRelationName = conjunction.getAtoms()[atomIndex].getPredicate().getName();
				String rightAttributeName = QueryAtom.findAtomFor(queryAtoms,rightRelationName).getAttributeAtIndex(aec.getOther()-shift).getName();
				query += dbNameDot + atomLeft.getRelation().getName() + "." + atomLeft.getAttributeAtIndex(aec.getPosition()).getName() + " = " + 
						dbNameDot + rightRelationName + "." + rightAttributeName;
			}
		}
		
		this.setSqlQueryString(query);		
	}
	
	private String getTableNamesPartOfQuery(Collection<QueryAtom> atoms, String databaseName) {
		List<String> relations = new ArrayList<>();
		for (QueryAtom a:atoms) {
			relations.add(databaseName + "." + a.getRelation().getName());
		}
		
		return Joiner.on(",").join(relations); 
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
		return "SQLQuery - " + super.toString() + " / SQL: " + sqlQueryString;
	}

}
