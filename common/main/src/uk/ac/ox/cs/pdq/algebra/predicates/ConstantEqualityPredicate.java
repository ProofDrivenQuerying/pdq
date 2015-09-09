package uk.ac.ox.cs.pdq.algebra.predicates;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.util.Tuple;

import com.google.common.base.Objects;

/**
 * Compares the value at a given position in a tuple with a value given by a
 * constant.
 *
 * @author Julien Leblay
 */
public class ConstantEqualityPredicate implements EqualityPredicate {

	private static Logger log = Logger.getLogger(ConstantEqualityPredicate.class);

	/** The position on which the predicate is evaluated */
	private final int position;

	/** The value to which the tuple must be equals at the given position */
	private final TypedConstant<?> constant;

	/** The value to which the tuple must be equals at the given position */
	private final Object value;

	/**
	 * Default construction
	 * @param position
	 * @param value
	 */
	public ConstantEqualityPredicate(int position, TypedConstant<?> constant) {
		assert position >= 0 : 
				"Attribute positions to compare cannot be negative in equality predicate.";
		this.position = position;
		this.constant = constant;
		this.value = constant != null ? constant.getValue() : null;
	}

	/**
	 * @param t
	 * @return true if the tuple t satisfies the predicate
	 * @see uk.ac.ox.cs.pdq.algebra.predicates.Predicate#isSatisfied(Tuple)
	 */
	@Override
	public boolean isSatisfied(Tuple t) {
		assert t.size() > this.position : 
				"Tuple must comply for bound given by the predicate positions";
		Object sourceValue = t.getValue(this.position);
		Object targetValue = this.value;
		if (sourceValue == null) {
			return targetValue == null;
		}
		if (sourceValue instanceof Comparable<?> && targetValue instanceof Comparable<?>) {
			try {
				Method m = Comparable.class.getMethod("compareTo", Object.class);
				return ((int) m.invoke(sourceValue, targetValue)) == 0;
			} catch (NoSuchMethodException | SecurityException
					| IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				log.warn("Problem comparing " + sourceValue + " to " + targetValue + ": " + e);
			}
		}

		return sourceValue.equals(targetValue);
	}

	/**
	 * @return the position of the attribute to compare
	 * @see uk.ac.ox.cs.pdq.algebra.predicates.EqualityPredicate#getPosition()
	 */
	@Override
	public int getPosition() {
		return this.position;
	}

	/**
	 * @return the constant value to compare with
	 */
	public TypedConstant<?> getValue() {
		return this.constant;
	}
	
	/**
	 * @param predicate the (possibly nested) predicate to flatten, if null the empty collection is returned.
	 * @return a collection of predicate remove the nesting of conjunction that
	 * it may contain.
	 */
	public Collection<Predicate> flatten() {
		Collection<Predicate> result = new LinkedList<Predicate>();
		result.add(this);
		return result;
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
