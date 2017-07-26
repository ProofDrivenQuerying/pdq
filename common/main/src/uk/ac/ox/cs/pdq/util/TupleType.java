package uk.ac.ox.cs.pdq.util;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;

import uk.ac.ox.cs.pdq.db.TypedConstant;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

// TODO: Auto-generated Javadoc
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

//		/**
//		 * Creates a new Default object.
//		 *
//		 * @param typed List<? extends Typed>
//		 * @return TupleType
//		 */
//		public static TupleType createFromTyped(final List<? extends Typed> typed) {
//			return interner.intern(new TupleTypeImpl(typed));
//		}
	}
}