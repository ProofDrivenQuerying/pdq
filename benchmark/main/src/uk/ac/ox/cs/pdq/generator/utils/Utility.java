package uk.ac.ox.cs.pdq.generator.utils;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class Utility {

	/**
	 * Creates a relation with the given name, arity and all attributes of
	 * String type.
	 * @param name
	 * @param arity
	 * @return 
	 */
	public static Relation createStringsRelation(String name, int arity) {
		List<Attribute> attributes = new ArrayList<>();
		for (int index = 0; index < arity; ++index) {
			attributes.add(new Attribute(String.class, "x" + index));
		}
		return new Relation(name, attributes) {};
	}
}
