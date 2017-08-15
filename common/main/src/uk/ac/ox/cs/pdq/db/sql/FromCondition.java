package uk.ac.ox.cs.pdq.db.sql;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;

public class FromCondition {
	
	List<String> fromRelations = new ArrayList<>();

	public FromCondition(List<String> relations) {
		fromRelations = relations;
	}

	public String getConditionsSQLSubstring(String databaseName) {
		List<String> relations = new ArrayList<>();
		for (String r:fromRelations) {
			relations.add(databaseName + "." + r);
		}
		
		return Joiner.on(",").join(relations); 
	}

}
