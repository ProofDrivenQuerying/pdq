package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;

/**
 * @author Gabor
 *
 */
public class DifferenceQuery extends BasicSelect {
	private BasicSelect left;
	private BasicSelect right;

	public DifferenceQuery(BasicSelect left, BasicSelect right, Schema schema) {
		this.left = left;
		this.right = right;
		this.schema = schema;
		init();
	}
	public DifferenceQuery(ConjunctiveQuery leftQuery, ConjunctiveQuery rightQuery, Schema schema) {
		this.left = new BasicSelect(schema, leftQuery);
		this.right = new BasicSelect(schema, rightQuery);
		init();
	}

	private void init() {
		resultTerms = left.getResultTerms();
		formula = left.formula;
		List<String> differentConditions = new ArrayList<String>(right.whereConditions);
		differentConditions.removeAll(left.whereConditions);
		
		List<String> differentTableNames = new ArrayList<String>(right.fromTableNameNoAliases);
		differentTableNames.removeAll(left.fromTableNameNoAliases);
		List<String> nestedQueryTableNames = new ArrayList<String>();
		for (String s: differentTableNames) {
			nestedQueryTableNames.add(right.fromTableName.get(right.fromTableNameNoAliases.indexOf(s)));
		}
		
		String sqlQueryString = left.statements.get(0);
		if (left.whereConditions.isEmpty()) {
			sqlQueryString += " WHERE \n ";
		} else {
			sqlQueryString += " AND \n ";
		}
		sqlQueryString += " NOT EXISTS ( SELECT " + Joiner.on(",").join(right.select) + " FROM " + Joiner.on(",").join(nestedQueryTableNames);
		if (differentConditions.isEmpty()) {
			sqlQueryString += ")";
		} else {
			sqlQueryString += " WHERE " + Joiner.on(" AND ").join(differentConditions) + " )";
		}
		this.statements.add(sqlQueryString);
		for (String key: left.aliasKeyToTable.keySet()) {
			storeKeyValuePairs(key, left.aliasKeyToTable.get(key));
		}
		for (String key: right.aliasKeyToTable.keySet()) {
			if (!aliasKeyToTable.containsKey(key))
				storeKeyValuePairs(key, right.aliasKeyToTable.get(key));
		}
	}

}
