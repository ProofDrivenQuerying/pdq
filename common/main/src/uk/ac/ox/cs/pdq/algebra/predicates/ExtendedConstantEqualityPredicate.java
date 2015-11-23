package uk.ac.ox.cs.pdq.algebra.predicates;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;


/**
 * Extends an ConstantEqualityPredicate by keeping the relations of the referenced attributes 
 * @author Efthymia Tsamoura
 *
 */
public class ExtendedConstantEqualityPredicate extends ConstantEqualityPredicate{

	private final Relation relation;
	
	private final String alias;
	
	public ExtendedConstantEqualityPredicate(int position, TypedConstant<?> constant, Relation relation,  String alias) {
		super(position, constant);
		Preconditions.checkNotNull(relation);
		Preconditions.checkArgument(position >= 0 && position < relation.getArity());
		this.relation = relation;
		this.alias = alias;
	}

	public Attribute getAttribute() {
		return this.relation.getAttribute(this.getPosition());
	}
	
	public Relation getRelation() {
		return this.relation;
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
				&& this.getPosition() == ((ExtendedConstantEqualityPredicate) o).getPosition()
				&& (this.getValue() == null ? o == null : this.getValue().equals(((ExtendedConstantEqualityPredicate) o).getValue()))
				&& this.getRelation() == ((ExtendedConstantEqualityPredicate) o).getRelation();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(this.getPosition(), this.getValue(), this.getRelation());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.alias==null ? this.relation.getName():this.alias).append(".").append(this.getAttribute().getName()).append('=');
		result.append("'").append(this.getValue()).append("'");
		return result.toString();
	}

}
