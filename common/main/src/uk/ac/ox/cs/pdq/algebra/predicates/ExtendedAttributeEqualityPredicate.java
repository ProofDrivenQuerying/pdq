package uk.ac.ox.cs.pdq.algebra.predicates;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;


/**
 * Extends an AttributeEqualityPredicate by keeping the relations of the referenced attributes 
 * @author Efthymia Tsamoura
 *
 */
public class ExtendedAttributeEqualityPredicate extends AttributeEqualityPredicate {

	private final Relation left;
	
	private final Relation right;
	
	private final String leftAlias;
	
	private final String rightAlias;
	
	public ExtendedAttributeEqualityPredicate(int position, int other, Relation left, String leftAlias, Relation right, String rightAlias) {
		super(position, other);
		Preconditions.checkNotNull(left);
		Preconditions.checkNotNull(right);
		Preconditions.checkArgument(position >= 0 && position < left.getArity());
		Preconditions.checkArgument(other >= 0 && other < right.getArity());
		this.left = left;
		this.right = right;
		this.leftAlias = leftAlias;
		this.rightAlias = rightAlias;
	}

	public Relation getLeft() {
		return this.left;
	}

	public Relation getRight() {
		return this.right;
	}
	
	public Attribute getLeftAttribute() {
		return this.left.getAttribute(this.getPosition());
	}
	
	public Attribute getRightAttribute() {
		return this.right.getAttribute(this.getOther());
	}
	
	public String getLeftAlias() {
		return this.leftAlias;
	}
	
	public String getRightAlias() {
		return this.rightAlias;
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
				&& this.getPosition() == ((ExtendedAttributeEqualityPredicate) o).getPosition()
				&& this.getOther() == ((ExtendedAttributeEqualityPredicate) o).getOther()
				&& this.getLeft() == ((ExtendedAttributeEqualityPredicate) o).getLeft()
				&& this.getRight() == ((ExtendedAttributeEqualityPredicate) o).getRight();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(this.getPosition(), this.getOther(),  this.getLeft(), this.getRight());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.leftAlias==null ? this.left.getName():this.leftAlias).append(".").append(this.getLeftAttribute().getName()).append('=');
		result.append(this.rightAlias==null ? this.right.getName():this.rightAlias).append(".").append(this.getRightAttribute().getName());
		return result.toString();
	}

}
