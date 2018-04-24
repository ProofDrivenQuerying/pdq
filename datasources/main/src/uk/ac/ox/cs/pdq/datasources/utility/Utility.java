package uk.ac.ox.cs.pdq.datasources.utility;

import java.lang.reflect.Type;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.TupleType;

public class Utility {

	/**
	 * Generates a list of terms matching the attributes of the input relation.
	 *
	 * @param relation Relation
	 * @return List<Term>
	 */
	public static Term[] createVariables(Relation relation) {
		Term[] result = new Term[relation.getArity()];
		for (int i = 0, l = relation.getArity(); i < l; i++) 
			result[i] = Variable.getFreshVariable();
		return result;
	}
	
	/**
	 * Creates a new Default object.
	 *
	 * @param typed List<? extends Typed>
	 * @return TupleType
	 */
	public static TupleType createFromTyped(Attribute[] typed) {
		Type[] types = new Type[typed.length];
		for(int attributeIndex = 0; attributeIndex < typed.length; ++attributeIndex) 
			types[attributeIndex] = typed[attributeIndex].getType();
		return TupleType.DefaultFactory.create(types);
	}
}
