package uk.ac.ox.cs.pdq.db;

import java.lang.reflect.Type;
import java.util.Objects;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.base.Preconditions;

/**
 * Schema constant
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class TypedConstant<T> implements Typed, Constant {

	/** The constant's type */
	private final Type type;

	/** The constant's value */
	private final T value;

	/** Cached instance hash (only possible because variables are immutable). */
	private int hash = Integer.MIN_VALUE;

	/** Cached String representation of a variable. */
	private String rep = null;

	/**
	 * Default constructor
	 * @param value
	 *            The constant's value
	 */
	public TypedConstant(T value) {
		Preconditions.checkArgument(value != null);
		this.type = value.getClass();
		this.value = value;
	}

	/**
	 * Copy constructor
	 * @param constant
	 */
	public TypedConstant(TypedConstant<T> constant) {
		this.type = constant.type;
		this.value = constant.value;
	}

	/**
	 * @return Class<T>
	 * @see uk.ac.ox.cs.pdq.util.Typed#getType()
	 */
	@Override
	public Type getType() {
		return this.type;
	}

	/**
	 * @return T
	 */
	public T getValue() {
		return this.value;
	}

	/**
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
				&& this.value.equals(((TypedConstant<?>) o).value);

	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		if (this.hash == Integer.MIN_VALUE) {
			this.hash = Objects.hash(this.value, this.type);
		}
		return this.hash;
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		if (this.rep == null) {
			StringBuilder result = new StringBuilder();
			result.append(this.value+this.type.toString());
			this.rep = result.toString().intern();
		}
		return this.rep;
	}

	/**
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.fol.Term#isVariable()
	 */
	@Override
	public boolean isVariable() {
		return false;
	}

	/**
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.fol.Term#isSkolem()
	 */
	@Override
	public boolean isSkolem() {
		return false;
	}
}
