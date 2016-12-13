package uk.ac.ox.cs.pdq.algebra2;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Join is a top-level class for all join operators.
 *
 * @author Julien Leblay
 */
public class Join implements RelationalOperator {

	/**
	 * Variant is an enumeration of the types of different joins.
	 */
	public static enum Variants {
		/** The nested loop join. */
		NESTED_LOOP, 
		/** The merge join. */
		MERGE, 
		/** The asymmetric hash join. */
		ASYMMETRIC_HASH, 
		/** The symmetric hash join. */
		SYMMETRIC_HASH
	}

	/** The variant. */
	protected Variants variant = Variants.SYMMETRIC_HASH;
	
	private final Predicate predicate;
	
	/**
	 * Instantiates a new join.
	 *
	 * @param pred Atom
	 * @param children the children
	 */
	public Join(Predicate predicate) {
		Preconditions.checkNotNull(predicate);
		this.predicate = predicate;
	}

	/**
	 * Gets the join variant.
	 *
	 * @return Variants
	 */
	public Variants getVariant() {
		return this.variant;
	}

	/**
	 * Sets the join variant.
	 *
	 * @param variant Variants
	 */
	public void setVariant(Variants variant) {
		Preconditions.checkNotNull(variant);
		this.variant = variant;
	}

	/**
	 * Gets the predicate associated with this join. If the join is natural this returns null.
	 *
	 * @return Atom
	 * @see uk.ac.ox.cs.pdq.algebra.PredicateBasedOperator#getPredicate()
	 */
	public Predicate getPredicate() {
		return this.predicate;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.algebra.RelationalOperator#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName());
		result.append('_').append(this.variant);
		if (this.predicate != null) {
			result.append(this.predicate);
		}
		return result.toString();
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
				&& this.predicate == ((Join) o).predicate;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(this.predicate);
	}

	@Override
	public int getArity() {
		// TODO Auto-generated method stub
		return 2;
	}

}