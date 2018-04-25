package uk.ac.ox.cs.pdq.util;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TypedConstant;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

/**
 * Represents a type of tuple. Used to define a type of tuple and then create
 * tuples of that type.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public interface TupleType extends Serializable{

	/**
	 * Size.
	 *
	 * @return int
	 */
	int size();

	/**
	 * Gets the type.
	 *
	 * @param i int
	 * @return Class<?>
	 */
	Type getType(int i);

	/**
	 * Gets the types.
	 *
	 * @return Class<?>[]
	 */
	Type[] getTypes();

	/**
	 * Tuple are immutable objects. Tuples should contain only immutable objects
	 * or objects that won't be modified while part of a tuple.
	 *
	 * @param values the values
	 * @return Tuple with the given values
	 * @throws IllegalArgumentException             if the wrong # of arguments or incompatible tuple values are
	 *             provided
	 */
	Tuple createTuple(Object... values);

	/**
	 * Creates a tuple from a list of constants.
	 *
	 * @param values the values
	 * @return Tuple
	 */
	Tuple createTuple(List<TypedConstant> values);

	/**
	 * Creates a tuple by appending the given left and right tuples.
	 * @param left Tuple
	 * @param right Tuple
	 * @return Tuple
	 */
	Tuple appendTuples(Tuple left, Tuple right);

	/**
	 * Creates a tuple type by appending the given left and right sub-types.
	 * @param right TupleType
	 * @return a new type
	 */
	TupleType append(TupleType right);

	/**
	 * Checks if is assignable from.
	 *
	 * @param other the other
	 * @return true of each of this TypleType's individual type is assignable
	 * from the given other individual type of the same index.
	 */
	boolean isAssignableFrom(TupleType other);

	/**
	 * Checks if is instance.
	 *
	 * @param other the other
	 * @return true of each of this given tuple's items can be assigned from
	 * from the corresponding class in the TupleType.
	 */
	boolean isInstance(Tuple other);

	/**
	 * Checks if is instance.
	 *
	 * @param values Object[]
	 * @return true of each of this given values can be assigned from
	 * from the corresponding class in the TupleType.
	 */
	boolean isInstance(Object... values);

	/** The empty tuple. */
	static final TupleType EmptyTupleType = DefaultFactory.create();

	// TODO: this is copied from uk.ac.ox.cs.pdq.datasources.utility;
	// Either remove it from that class or from here. 
	public static TupleType createFromTyped(Attribute[] typed) {
		Type[] types = new Type[typed.length];
		for(int attributeIndex = 0; attributeIndex < typed.length; ++attributeIndex) 
			types[attributeIndex] = typed[attributeIndex].getType();
		return TupleType.DefaultFactory.create(types);
	}

	/**
	 * Tuple factory.
	 *
	 * @author Julien Leblay
	 */
	public class DefaultFactory {
		
		/** The interner. */
		private static Interner<TupleType> interner = Interners.newStrongInterner();

		/**
		 * Creates the.
		 *
		 * @param types Class<?>[]
		 * @return TupleType
		 */
		public static TupleType create(final Type... types) {
			return interner.intern(new TupleTypeImpl(types));
		}

		/**
		 * Creates a new Default object.
		 *
		 * @param typed List<? extends Typed>
		 * @return TupleType
		 */
		public static TupleType createFromTyped(final Typed... typed) {
			return interner.intern(new TupleTypeImpl(typed));
		}
		
		/**
		 * Creates a new Default object.
		 *
		 * @param attributes List<? extends Typed>
		 * @return TupleType
		 */
		public static TupleType createFromTyped(final Attribute[] attributes) {
			Type[] types = new Type[attributes.length];
			for(int index = 0; index < attributes.length; ++index) 
				types[index] = attributes[index].getType();
			return interner.intern(new TupleTypeImpl(types));
		}
	}
}