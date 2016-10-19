package uk.ac.ox.cs.pdq.db;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.io.xml.QNames;

/**
 * 
 * @author george k
 *
 */
public class DatabaseEqualityRelation extends DatabaseRelation {

	public final static DatabaseRelation relation = getDatabaseEqualityRelation();


	private DatabaseEqualityRelation(String name, List<Attribute> attributes) {
		super(name, attributes);
	}

	/** The Constant serialVersionUID. */
	private final static long serialVersionUID = 3503553786085749666L;


	/** A FactID attribute. THIS SHOULD DISAPPEAR */
	public final static Attribute Fact = new Attribute(Integer.class, "Fact");

	private static DatabaseRelation getDatabaseEqualityRelation() {		
		List<Attribute> attributes = new ArrayList<>();
		attributes.add(new Attribute(String.class, DatabaseRelation.attrPrefix + 0));
		attributes.add(new Attribute(String.class, DatabaseRelation.attrPrefix + 1));
		attributes.add(DatabaseRelation.Fact);
		return new DatabaseEqualityRelation(QNames.EQUALITY.toString(), attributes);
	}	



}
