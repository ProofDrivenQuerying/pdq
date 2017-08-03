package uk.ac.ox.cs.pdq.db.sql;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;

public class FromCondition {
	
	List<String> fromRelations = new ArrayList<>();

	public FromCondition(List<String> relations) {
		fromRelations = relations;
	}

	public String getConditionsSQLSubstring() {
		return Joiner.on(",").join(fromRelations); 
	}

}
