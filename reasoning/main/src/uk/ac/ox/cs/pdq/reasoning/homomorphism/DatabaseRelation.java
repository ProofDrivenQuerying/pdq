package uk.ac.ox.cs.pdq.reasoning.homomorphism;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.io.xml.QNames;

/**
 * Relations stored in the database built up for homomorphism detection.
 *
 * @author Efthymia Tsamoura
 */
public class DatabaseRelation extends Relation {
	
	/** The Constant serialVersionUID. */
	private final static long serialVersionUID = 3503553786085749666L;
	
	/** The attr prefix. */
	private final static String attrPrefix = "x";
	
	/** The Fact. */
	public final static Attribute Fact = new Attribute(Integer.class, "Fact");

	/**
	 * Constructor for DBRelation.
	 * @param name String
	 * @param attributes List<Attribute>
	 */
	public DatabaseRelation(String name, List<Attribute> attributes) {
		super(name, attributes);
	}
	
	/**
	 * Creates the db relation.
	 *
	 * @param relation the relation
	 * @return a new database relation with attributes x0,x1,...,x_{N-1}, Fact where
	 *         x_i maps to the i-th relation's attribute
	 */
	public static DatabaseRelation createDatabaseRelation(Relation relation) {
		List<Attribute> attributes = new ArrayList<>();
		for (int index = 0, l = relation.getAttributes().size(); index < l; ++index) {
			attributes.add(new Attribute(String.class, DatabaseRelation.attrPrefix + index));
		}
		attributes.add(DatabaseRelation.Fact);
		return new DatabaseRelation(relation.getName(), attributes);
	}

	public static DatabaseRelation createEqualityTable() throws SQLException {		
		List<Attribute> attributes = new ArrayList<>();
		attributes.add(new Attribute(String.class, DatabaseRelation.attrPrefix + 0));
		attributes.add(new Attribute(String.class, DatabaseRelation.attrPrefix + 1));
		attributes.add(DatabaseRelation.Fact);
		return new DatabaseRelation(QNames.EQUALITY.toString(), attributes);
	}
}

