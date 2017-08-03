package uk.ac.ox.cs.pdq.db.sql;

import java.util.LinkedHashMap;

import com.google.common.base.Joiner;

import uk.ac.ox.cs.pdq.fol.Variable;

public class SelectCondition {
	
	LinkedHashMap<String,Variable> projections;

	public SelectCondition(LinkedHashMap<String,Variable> projections) {
		this.projections = projections;
	}

	public String getConditionsSQLSubstring() {
		return Joiner.on(",").join(projections.keySet()); 
	}

	public LinkedHashMap<String, Variable> getInternalMap() {
		return projections;
	}

}
