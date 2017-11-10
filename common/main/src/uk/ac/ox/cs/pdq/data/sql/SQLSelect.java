package uk.ac.ox.cs.pdq.data.sql;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Joiner;

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
	protected List<String> select = new ArrayList<>();
	
	public SQLSelect(ConjunctiveQuery source, SqlDatabaseInstance instance) {
		super(source, instance.schema);
		initSelect(source, instance.databaseParameters.getDatabaseName());
		initFrom(source, instance.databaseParameters.getDatabaseName());
		initWhere(source, instance.databaseParameters.getDatabaseName());
		sqlQueryString = "SELECT " + Joiner.on(",").join(select) + " FROM " + Joiner.on(",").join(fromTableName);
		if (!whereConditions.isEmpty())
			sqlQueryString += " WHERE " + Joiner.on(" AND ").join(whereConditions);
				
	}

	private void initSelect(ConjunctiveQuery source, String databaseName) {
		// SELECT free variables			
		for (ConjunctiveQueryDescriptor a: this.getQueryAtoms()) {
			// loop over all atoms of the query (flattened hierarchy)
			for (Variable v: a.getFreeVariableToPosition().keySet()) {
				// loop over all free variables of current Atom.
				select.add(databaseName + "." + a.getRelation().getName() + "." + a.getAttributeAtIndex(a.getFreeVariableToPosition().get(v)).getName());
			}
		}
	}
	
	private void initFrom(ConjunctiveQuery source, String databaseName) {
		// FROM table names			
		for (ConjunctiveQueryDescriptor a:this.getQueryAtoms()) {
			// from each table of the query
			fromTableName.add(databaseName + "." + a.getRelation().getName());
		}
	}
	
	private void initWhere(ConjunctiveQuery source, String databaseName) {
		// WHERE CONSTANT EQUALITY CONDITIONS
		for (ConjunctiveQueryDescriptor a: this.getQueryAtoms()) {
			if (a.hasConstantEqualityCondition()) {
				for (Attribute attribute : a.getConstantEqualityConditions().keySet()) {
					whereConditions.add(databaseName + "." +a.getRelation().getName() + "." + attribute.getName() + " = " + SqlDatabaseInstance.convertTermToSQLString(attribute, a.getConstantEqualityConditions().get(attribute)));
				}
			}
		}
		
		// WHERE ATTRIBUTE EQUALITY CONDITIONS
		for (Conjunction conjunction: getMappedConjunctiveQuery().keySet()) {
			if (getMappedConjunctiveQuery().get(conjunction).getMatchingColumnIndexes().isEmpty())
				continue;
			for (Pair<Integer,Integer> matchingIndexes:getMappedConjunctiveQuery().get(conjunction).getMatchingColumnIndexes()) {
				ConjunctiveQueryDescriptor atomLeft = getMappedConjunctiveQuery().get(conjunction).getLeftAtom();// ConjunctiveQueryDescriptor.findAtomFor(queryAtoms, conjunction.getAtoms()[0].getPredicate().getName());
				int shift = 0;
				int atomIndex =1;
				while (matchingIndexes.getRight()>=shift + conjunction.getAtoms()[atomIndex].getTerms().length) {
					shift += conjunction.getAtoms()[atomIndex].getTerms().length;
					atomIndex++;
				}
				String rightRelationName = conjunction.getAtoms()[atomIndex].getPredicate().getName();
				String rightAttributeName = ConjunctiveQueryDescriptor.findAtomFor(queryAtoms,rightRelationName).getAttributeAtIndex(matchingIndexes.getRight()-shift).getName();
				String newCondition = databaseName + "." + atomLeft.getRelation().getName() + "." + atomLeft.getAttributeAtIndex(matchingIndexes.getLeft()).getName() + " = " + 
						databaseName + "." + rightRelationName + "." + rightAttributeName;
				whereConditions.add(newCondition);
			}
		}
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
