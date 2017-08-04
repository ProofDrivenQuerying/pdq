package uk.ac.ox.cs.pdq.db.sql;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;


public class WhereCondition {
	
	List<String> conds;

	public WhereCondition() {
		conds = new ArrayList<String>();
	}
	
	public WhereCondition(List<String> conditions) {
		conds = conditions;
	}

	public String getConditionsSQLSubstring() {
		return Joiner.on(" AND ").join(conds); 
	}
	
	private List<String> getConditionsStrings() {
		return conds; 
	}

	public void addCondition(WhereCondition other) {
		conds.addAll(other.getConditionsStrings());
	}

	public boolean isEmpty() {
		return conds.isEmpty();
	}

}
