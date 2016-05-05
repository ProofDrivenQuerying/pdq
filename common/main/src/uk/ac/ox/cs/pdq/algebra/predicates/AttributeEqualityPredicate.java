package uk.ac.ox.cs.pdq.algebra.predicates;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.util.Tuple;

import com.google.common.base.Objects;


/**
 * Compares the values at two given positions in a tuple.
 *
 * @author Julien Leblay
 */
public class AttributeEqualityPredicate implements EqualityPredicate {

	/** The log. */
	private static Logger log = Logger.getLogger(AttributeEqualityPredicate.class);

	/**  The first of the two positions to be compared for equality. */
	private final int position;

	/**  The other position to which position must be equals for a given tuple. */
	private final int other;

	/**
	 * Constructor for AttributeEqualityPredicate.
	 * @param position int
	 * @param other int
	 */
	public AttributeEqualityPredicate(int position, int other) {
		assert position >= 0 && other >= 0 : 
				"Attribute positions to compare cannot be negative in equality predicate.";
		this.position = position;
		this.other = other;
	}

	/**
	 * Checks if the values in the two positions are the same.
	 *
	 * @param t the t
	 * @return true if the tuple t satisfies the predicate
	 * @see uk.ac.ox.cs.pdq.algebra.predicates.Predicate#isSatisfied(Tuple)
	 */
	@Override
	public boolean isSatisfied(Tuple t) {
		assert t.size() > this.position && t.size() > this.other : 
				"Tuple must comply for bound given by the predicate positions";
		try {
			Object sourceValue = t.getValue(this.position);
			Object targetValue = t.getValue(this.other);
			if (sourceValue == null) {
				return t.getType().getType(this.position)
						.equals(t.getType().getType(this.other))
						&& targetValue == null;
			}
			if (sourceValue instanceof Comparable<?> && targetValue instanceof Comparable<?>) {
				try {
					Method m = Comparable.class.getMethod("compareTo", Object.class);
					return ((int) m.invoke(sourceValue, targetValue)) == 0;
				} catch (NoSuchMethodException | SecurityException
						| IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					log.warn(e.getMessage());
				}
			}
			return sourceValue.equals(targetValue);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}

		return false;
	}
	

	/**
	 * Gets the first position to compare.
	 *
	 * @return the first position of the attribute to compare
	 * @see uk.ac.ox.cs.pdq.algebra.predicates.EqualityPredicate#getPosition()
	 */
	@Override
	public int getPosition() {
		return this.position;
	}

	/**
	 * Gets the other position to compare.
	 *
	 * @return the second position of the attribute to compare
	 */
	public int getOther() {
		return this.other;
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
				&& this.position == ((AttributeEqualityPredicate) o).position
				&& this.other == ((AttributeEqualityPredicate) o).other;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(this.position, this.other);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append('#').append(this.position).append('=');
		result.append('#').append(this.other);
		return result.toString();
	}
}
