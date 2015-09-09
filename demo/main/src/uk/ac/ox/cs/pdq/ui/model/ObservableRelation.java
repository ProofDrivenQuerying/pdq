package uk.ac.ox.cs.pdq.ui.model;

import uk.ac.ox.cs.pdq.db.Relation;


public class ObservableRelation extends Relation {

	private Relation relation;
	
	public ObservableRelation(Relation relation) {
		super(relation);
		this.relation = relation;
	}

	@Override
	public String toString() {
		String result = String.valueOf(this.relation);
		if (result.length() > 128) {
			result = result.substring(125) + "...";
		}
		return result;
	}
}
