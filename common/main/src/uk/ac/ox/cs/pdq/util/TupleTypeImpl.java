package uk.ac.ox.cs.pdq.util;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.NotImplementedException;

import uk.ac.ox.cs.pdq.db.DataType;
import uk.ac.ox.cs.pdq.db.TypedConstant;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * A tuple type implementation.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
class TupleTypeImpl implements TupleType {
	private static final long serialVersionUID = 1418501776722101770L;
	/**  The internal tuple type representation. */
	private final Type[] types;

	/**
	 * Default constructor.
	 *
	 * @param types the types
	 */
	TupleTypeImpl(Type... types) {
		this.types = (types != null ? types : new Class<?>[0]);
	}

	/**
	 * Constructs a tuple table from a list of typed (e.g. attributes or
	 * constants) rather than an array of classes.
	 *
	 * @param items the items
	 */
	TupleTypeImpl(List<? extends Typed> items) {
		this(toClassArray(items));
	}

	/**
	 * To class array.
	 *
	 * @param items the items
	 * @return an array of Class corresponding to the given list of typed objects.
	 */
	private static Type[] toClassArray(List<? extends Typed> items) {
		if (items == null) 
			return null;
		Type[] result = new Type[items.size()];
		int i = 0;
		for (Typed a : items) 
			result[i++] = a.getType();
		return result;
	}

	/**
	 * Size.
	 *
	 * @return the number of sub-typed in the tuple type.
	 * @see uk.ac.ox.cs.pdq.util.TupleType#size()
	 */
	@Override
	public int size() {
		return this.types.length;
	}

	/**
	 * Gets the type.
	 *
	 * @param i int
	 * @return the type of the ith element in the tuple type.
	 * @see uk.ac.ox.cs.pdq.util.TupleType#getType(int)
	 */
	@Override
	public Type getType(int i) {
		return this.types[i];
	}

	/**
	 * Gets the types.
	 *
	 * @return the underlying array of types.
	 * @see uk.ac.ox.cs.pdq.util.TupleType#getTypes()
	 */
	@Override
	public Type[] getTypes() {
		return this.types;
	}

	/**
	 * Creates the tuple.
	 *
	 * @param values Object[]
	 * @return a fresh tuple, with this tuple type as type, from the give array
	 *  of objects.
	 * @see uk.ac.ox.cs.pdq.util.TupleType#createTuple(Object[])
	 */
	@Override
	public Tuple createTuple(Object... values) {
		if ((values == null && this.types.length != 0) || (values != null && values.length != this.types.length)) {
			throw new IllegalArgumentException(
					"Expected " + this.types.length + " values, not "
							+ (values == null ? "(null)" : values.length));
		}
		if (values == null || values.length == 0) {
			return Tuple.EmptyTuple;
		}
		if (!this.isInstance(values)) {
			throw new IllegalArgumentException("Cannot assign " +
					Lists.newArrayList(values) + " to " + this);
		}
		return new TupleImpl(this, values);
	}

	/**
	 * Creates the tuple.
	 *
	 * @param values List<TypedConstant>
	 * @return a fresh tuple, with this tuple type as type, from the give list
	 *  of constants.
	 * @see uk.ac.ox.cs.pdq.structures.TupleType#createTuple(List<TypedConstant>)
	 */
	@Override
	public Tuple createTuple(List<TypedConstant> values) {
		Object[] objects = new Object[values.size()];
		int i = 0;
		for (TypedConstant c: values) {
			objects[i++] = c.getValue();
		}

		return this.createTuple(objects);
	}

	/**
	 * Append tuples.
	 *
	 * @param left Tuple
	 * @param right Tuple
	 * @return a fresh tuple, made by appending the given left and right tuples.
	 * @see uk.ac.ox.cs.pdq.util.TupleType#appendTuples(Tuple, Tuple)
	 */
	@Override
	public Tuple appendTuples(Tuple left, Tuple right) {
		Preconditions.checkArgument(left.size() + right.size() == this.types.length);
		Object[] objects = new Object[this.types.length];
		System.arraycopy(left.getValues(), 0, objects, 0, left.size());
		System.arraycopy(right.getValues(), 0, objects, left.size(), right.size());
		return this.createTuple(objects);
	}

	/**
	 * Append.
	 *
	 * @param right TupleType
	 * @return a fresh tuple type, made by appending the given left and right sub-types.
	 * @see uk.ac.ox.cs.pdq.util.TupleType#append(TupleType)
	 */
	@Override
	public TupleType append(TupleType right) {
		Class<?>[] classes = new Class<?>[this.size() + right.size()];
		System.arraycopy(this.types, 0, classes, 0, this.size());
		System.arraycopy(right.getTypes(), 0, classes, this.size(), right.size());
		return new TupleTypeImpl(classes);
	}

	/**
	 * Checks if is assignable from.
	 *
	 * @param other TupleType
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.util.TupleType#isAssignableFrom(TupleType)
	 */
	@Override
	public boolean isAssignableFrom(TupleType other) {
		for (int i = 0; i < this.types.length; i++) {
			final Type nthType = this.types[i];
			final Type nthOther = other.getType(i);
			assert nthType != null;
			assert nthOther != null;
			// TODO: find an efficient way to type check accounting for (un)boxing
			if (nthType instanceof Class
					&& nthOther instanceof Class
					&& !((Class<?>) nthType).isAssignableFrom((Class<?>) nthOther)) {
				return false;
			}
			if (nthType != nthOther) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if is instance.
	 *
	 * @param other Tuple
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.util.TupleType#isInstance(Tuple)
	 */
	@Override
	public boolean isInstance(Tuple other) {
		return this.isInstance(other.getValues());
	}

	/**
	 * Checks if is instance.
	 *
	 * @param values Object[]
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.util.TupleType#isInstance(Object[])
	 */
	@Override
	public boolean isInstance(Object... values) {
		for (int i = 0; i < this.types.length; i++) {
			final Type nthType = this.types[i];
			assert nthType != null;
			final Object nthValue = values[i];
			if (nthValue != null) {
				if (nthType instanceof Class
						&& !((Class<?>) nthType).isAssignableFrom(nthValue.getClass())) {
					return false;
				} else if (nthType instanceof DataType
						&& !(((DataType) nthType).isAssignableFrom(nthValue))) {
					return false;
				} else if (!(nthType instanceof Type)) {
					throw new NotImplementedException("");
				}
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash((Object[]) this.types);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !TupleTypeImpl.class.isAssignableFrom(o.getClass())) {
			return false;
		}
		TupleType that = (TupleType) o;
		if (this.size() != that.size()) {
			return false;
		}
		for (int i = 0, l = this.types.length; i < l; i++) {
			if (!this.getType(i).equals(that.getType(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * To string.
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		return Arrays.toString(this.types);
	}
}
