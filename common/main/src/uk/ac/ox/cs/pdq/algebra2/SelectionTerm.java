package uk.ac.ox.cs.pdq.algebra2;

import java.util.List;
import java.util.Objects;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class SelectionTerm extends RelationalTerm {

	private final RelationalTerm child;

	/** The predicate associated with this selection. */
	private final Predicate predicate;

	/**  Cashed string representation. */
	private String toString = null;

	/** The hash. */
	private Integer hash = null;

	public SelectionTerm(Predicate predicate, RelationalTerm child) {
		super(child.getInputAttributes(), child.getOutputAttributes());
		Preconditions.checkNotNull(predicate);
		Preconditions.checkNotNull(child);
		for(Predicate p:predicate.getEqualityPredicates()) {
			if(p instanceof AttributeEqualityPredicate) {
				Preconditions.checkArgument(((AttributeEqualityPredicate)p).getPosition() < child.getOutputAttributes().size());
				Preconditions.checkArgument(((AttributeEqualityPredicate)p).getOther() < child.getOutputAttributes().size());
			}
			else if(p instanceof ConstantEqualityPredicate) {
				Preconditions.checkArgument(((ConstantEqualityPredicate)p).getPosition() < child.getOutputAttributes().size());
			}
		}
		this.predicate = predicate;
		this.child = child;
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
		if(this.toString == null) {
			StringBuilder result = new StringBuilder();
			result.append("Select");
			result.append('{');
			result.append('[').append(this.predicate).append(']');
			result.append(this.child.toString());
			result.append('}');
			this.toString = result.toString();
		}
		return this.toString;
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
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return super.equals(o)
				&& this.getClass().isInstance(o)
				&& this.predicate.equals(((SelectionTerm) o).predicate)
				&& this.child.equals(((SelectionTerm) o).child);
	}

	@Override
	public int hashCode() {
		if(this.hash == null) {
			this.hash = Objects.hash(this.predicate, this.child);
		}
		return this.hash;
	}

	@Override
	public List<RelationalTerm> getChildren() {
		return ImmutableList.of(this.child);
	}
}
