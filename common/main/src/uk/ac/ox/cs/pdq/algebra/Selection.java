package uk.ac.ox.cs.pdq.algebra;

import java.util.Objects;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.algebra.predicates.Predicate;

/**
 * OpenSelection operator.
 *
 * @author Julien Leblay
 */
public class Selection extends UnaryOperator implements PredicateBasedOperator {

	/** The predicate associated with this selection. */
	private final Predicate predicate;

	/**
	 * Instantiates a new selection.
	 *
	 * @param p Atom
	 * @param child            the child
	 */
	public Selection(Predicate p, RelationalOperator child) {
		super(child);
		Preconditions.checkNotNull(p);
		this.predicate = p;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.RelationalOperator#deepCopy()
	 */
	@Override
	public Selection deepCopy() throws RelationalOperatorException {
		return new Selection(this.predicate, this.child.deepCopy());
	}

	/**
	 * Gets the predicate associated with this selection. .
	 *
	 * @return the predicate of this selection
	 * @see uk.ac.ox.cs.pdq.algebra.PredicateBasedOperator#getPredicate()
	 */
	@Override
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
		result.append('(').append(this.child.toString()).append(')');
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
				&& this.predicate.equals(((Selection) o).predicate)
				&& this.child.equals(((Selection) o).child)
				;

	}


	@Override
	public int hashCode() {
		return Objects.hash(this.outputType, this.inputType, this.child,
				this.columns, this.inputTerms, this.predicate, this.metadata);
	}
}
