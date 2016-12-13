package uk.ac.ox.cs.pdq.algebra2;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.TypedConstant;

import com.google.common.base.Objects;

/**
 * Compares the value at a given position in a tuple with a value given by a
 * constant.
 *
 * @author Julien Leblay
 */
public class ConstantEqualityPredicate implements EqualityPredicate {

	/** The log. */
	private static Logger log = Logger.getLogger(ConstantEqualityPredicate.class);

	/**  The position of the tuple, whose value we will compare. */
	private final int position;

	/**  The value to which the tuple must equal at the given position. */
	private final TypedConstant<?> constant;

	/**  The value of the given position. */
	private final Object value;

	/**
	 * Default construction.
	 *
	 * @param position the position
	 * @param constant the constant
	 */
	public ConstantEqualityPredicate(int position, TypedConstant<?> constant) {
		assert position >= 0 : 
				"Attribute positions to compare cannot be negative in equality predicate.";
		this.position = position;
		this.constant = constant;
		this.value = constant != null ? constant.getValue() : null;
	}

	/**
	 * Gets the position of the tuple to compare.
	 *
	 * @return the position of the attribute to compare
	 * @see uk.ac.ox.cs.pdq.algebra.predicates.EqualityPredicate#getPosition()
	 */
	@Override
	public int getPosition() {
		return this.position;
	}

	/**
	 * Gets the value in the position of the tuple to compare.
	 *
	 * @return the constant value to compare with
	 */
	public TypedConstant<?> getValue() {
		return this.constant;
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
		if (o == null) {
			return false;
		}
		return this.getClass().isInstance(o)
				&& this.position == ((ConstantEqualityPredicate) o).position
				&& (this.value == null ? o == null : this.value.equals(((ConstantEqualityPredicate) o).value));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(this.position, this.value);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append('#').append(this.position).append('=');
		result.append(this.value);
		return result.toString();
	}
}
