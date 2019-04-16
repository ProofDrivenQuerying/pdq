package uk.ac.ox.cs.pdq.reasoningdatabase.sqlcommands;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;

/**
 * Represent a kind of nested select that will tell the difference between two
 * BasicSelects.
 * 
 * <pre>
 * Example: 
 * Left query: exists[x,y](R(x,y,z) & S(x,y))
 * Right query:exists[x,y,z](R(x,y,z) & (S(x,y) & T(z,res1,res2)))
 * 
 * The result will be all facts that only satisfy the first query, but not the second one.
 * 
 * </pre>
 * 
 * @author Gabor
 *
 */
public class DifferenceQuery extends BasicSelect {
	/**
	 * This select should have the same or more results then the right side query
	 * when executed independently.
	 */
	private BasicSelect left;
	/**
	 * This query should have the same conditions as the first one plus extra
	 * conditions for those "special cases" that we do not want in the result set.
	 */
	private BasicSelect right;

	/**
	 * Creates a Nested select based on two Basic selects in a way that the result
	 * will only contain facts matching the left side query but not the right side
	 * query.
	 */
	public DifferenceQuery(BasicSelect left, BasicSelect right, Schema schema) {
		this.left = left;
		this.right = right;
		this.schema = schema;
		init();
	}

	/**
	 * Same as above but for code simplicity this class can be created from two CQs
	 * directly.
	 * @throws DatabaseException in case the schema does not contain all relations referenced by the CQs.
	 */
	public DifferenceQuery(ConjunctiveQuery leftQuery, ConjunctiveQuery rightQuery, Schema schema) throws DatabaseException {
		this.left = new BasicSelect(schema, leftQuery);
		this.right = new BasicSelect(schema, rightQuery);
		init();
	}

	/**
	 * Uses the pre-processed BasicSelects with all the Select/From/Where parts.
	 */
	private void init() {

		// the resultTerms will be the same as the left side query's results.
		resultTerms = left.getResultTerms();

		// there is no formula that can describe the actual nested select, so we use the
		// left side formula instead. Not perfect, but since the resultTerms are the
		// same it will do just fine.
		formula = left.formula;
		// we need to identify the conditions that are different in the left and right
		// side queries. This will be the nested part of the DifferenceQuery.
		List<String> differentConditions = new ArrayList<String>(right.whereConditions);
		differentConditions.removeAll(left.whereConditions);

		// same with the table names, but here we have to pay attention to the aliases.
		List<String> differentTableNames = new ArrayList<String>(right.fromTableNameNoAliases);
		differentTableNames.removeAll(left.fromTableNameNoAliases);

		// we need to map from the table name to the the table's alias name.
		List<String> nestedQueryTableNames = new ArrayList<String>();
		for (String s : differentTableNames) {
			nestedQueryTableNames.add(right.fromTableName.get(right.fromTableNameNoAliases.indexOf(s)));
		}
		for (String fromPart:right.fromTableNameNoAliases) {
			String statementPart = right.fromTableName.get(right.fromTableNameNoAliases.indexOf(fromPart));
			if (left.fromTableNameNoAliases.indexOf(fromPart)>=0)
				statementPart = left.fromTableName.get(left.fromTableNameNoAliases.indexOf(fromPart));
			
			if (!nestedQueryTableNames.contains(statementPart) && left.fromTableNameNoAliases.indexOf(fromPart)==-1) {
				nestedQueryTableNames.add(statementPart);
			}
		}
		// now we can put together the sql statement string.
		// beggining with the left side query
		String sqlQueryString = left.statements.get(0);

		// continue with either a WHERE or if it is already there then just a new case
		// condition with an "AND" keyword...
		if (left.whereConditions.isEmpty()) {
			sqlQueryString += " WHERE \n ";
		} else {
			sqlQueryString += " AND \n ";
		}
		// Nested select part
		sqlQueryString += " NOT EXISTS ( SELECT " + Joiner.on(",").join(left.select) + " FROM " + Joiner.on(",").join(nestedQueryTableNames);

		// add the where conditions if there are any, and close the nested select
		if (differentConditions.isEmpty()) {
			sqlQueryString += ")";
		} else {
			sqlQueryString += " WHERE " + Joiner.on(" AND ").join(differentConditions) + " )";
		}

		// store the prepared statement string.
		this.statements.add(sqlQueryString);
		// store the mapping aliases. We can use the aliases prepared by the left and
		// right select if we add the left side first and then all new keys from the
		// right.
		for (String key : left.aliasKeyToTable.keySet()) {
			storeReplacementKeyValuePairs(key, left.aliasKeyToTable.get(key));
		}
		for (String key : right.aliasKeyToTable.keySet()) {
			if (!aliasKeyToTable.containsKey(key))
				storeReplacementKeyValuePairs(key, right.aliasKeyToTable.get(key));
		}
	}

}
