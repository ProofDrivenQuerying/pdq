package uk.ac.ox.cs.pdq.db;

import java.lang.reflect.Type;
import java.util.Objects;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * Schema constant.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 * @param <T> the generic type
 */
public class TypedConstant<T> extends Constant implements Typed  {

	/**  The constant's type. */
	private final Type type;

	/**  The constant's value. */
	public final T value;

	/** Cached instance hash (only possible because variables are immutable). */
	private int hash = Integer.MIN_VALUE;

	/** Cached String representation of a variable. */
	private String rep = null;

	/**
	 * Default constructor.
	 *
	 * @param value            The constant's value
	 */
	public TypedConstant(T value) {
		Preconditions.checkArgument(value != null);
		this.type = value.getClass();
		this.value = value;
	}

	/**
	 * Copy constructor.
	 *
	 * @param constant the constant
	 */
	public TypedConstant(TypedConstant<T> constant) {
		this.type = constant.type;
		this.value = constant.value;
	}
	
	/**
	 * Is untyped constant.
	 *
	 * @param zilch
	 */
	public boolean isUntypedConstant()
	{
		return false;
	}
	/**
	 * Gets the type.
	 *
	 * @return Class<T>
	 * @see uk.ac.ox.cs.pdq.util.Typed#getType()
	 */
	@Override
	public Type getType() {
		return this.type;
	}

	/**
	 * Gets the value.
	 *
	 * @return T
	 */
	public T getValue() {
		return this.value;
	}

	/**
	 * Equals.
	 *
	 * @param o Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return this.getClass().isInstance(o)
				&& this.value.equals(((TypedConstant<?>) o).value)
				&& this.type.equals(((TypedConstant<?>) o).type);

	}

	/**
	 * Hash code.
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
//		if (this.hash == Integer.MIN_VALUE) {
			this.hash = Objects.hash(this.value, this.type);
//		}
		return this.hash;
	}

	/**
	 * To string.
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		if (this.rep == null) {
			StringBuilder result = new StringBuilder();
			result.append(this.value);
			this.rep = result.toString().intern();
		}
		return this.rep;
	}

	/**
	 * Checks if is variable.
	 *
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.fol.Term#isVariable()
	 */
	@Override
	public boolean isVariable() {
		return false;
	}

	/**
	 * Checks if is skolem.
	 *
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.fol.Term#isSkolem()
	 */
// MR	@Override
	public boolean isSkolem() {
		return false;
	}
	
	@Override
	public TypedConstant<T> clone() {
		return new TypedConstant<T>(this.value);
	}
}
