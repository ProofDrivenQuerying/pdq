package uk.ac.ox.cs.pdq.algebra2;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;


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

	@Override
	public Collection<EqualityPredicate> getEqualityPredicates() {
		// TODO Auto-generated method stub
		return Lists.<EqualityPredicate>newArrayList(this);
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
