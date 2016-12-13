package uk.ac.ox.cs.pdq.algebra2;

import java.util.Objects;

import com.google.common.base.Preconditions;

/**
 * OpenSelection operator.
 *
 * @author Julien Leblay
 */
public class Selection implements RelationalOperator {

	/** The predicate associated with this selection. */
	private final Predicate predicate;

	/**
	 * Instantiates a new selection.
	 *
	 * @param p Atom
	 * @param child            the child
	 */
	public Selection(Predicate predicate) {
		Preconditions.checkNotNull(predicate);
		this.predicate = predicate;
	}

	/**
	 * Gets the predicate associated with this selection. .
	 *
	 * @return the predicate of this selection
	 * @see uk.ac.ox.cs.pdq.algebra.PredicateBasedOperator#getPredicate()
	 */
	public Predicate getPredicate() {
		return this.predicate;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName());
		result.append('{').append(this.predicate).append('}');
		return result.toString();
	}

	/**
	 * Two selection operators are equal if they have the same predicate and the same child operator 
	 * ("same" in both cases tested with the corresponding equals() method).
	 * 
	 * @param o Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		return super.equals(o)
				&& this.getClass().isInstance(o)
				&& this.predicate.equals(((Selection) o).predicate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.predicate);
	}

	@Override
	public int getArity() {
		// TODO Auto-generated method stub
		return 1;
	}
}
