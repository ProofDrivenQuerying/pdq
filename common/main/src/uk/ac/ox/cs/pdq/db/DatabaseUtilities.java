package uk.ac.ox.cs.pdq.db;

import uk.ac.ox.cs.pdq.util.TupleType;

public class DatabaseUtilities {

	public static TupleType getType(Relation relation) {
		return TupleType.DefaultFactory.createFromTyped(relation.getAttributes());
	}
}
