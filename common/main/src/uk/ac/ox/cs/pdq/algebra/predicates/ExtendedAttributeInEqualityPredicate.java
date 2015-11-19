package uk.ac.ox.cs.pdq.algebra.predicates;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.util.Tuple;


/**
 * An inequality predicate that keeps the relations of the referenced attributes 
 * @author Efthymia Tsamoura
 *
 */
public class ExtendedAttributeInEqualityPredicate extends ExtendedAttributeEqualityPredicate {
	
	public ExtendedAttributeInEqualityPredicate(int position, int other, Relation left, String leftAlias, Relation right, String rightAlias) {
		super(position, other, left, leftAlias, right, rightAlias);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getLeftAlias()==null ? this.getLeft().getName():this.getLeftAlias()).append(".").append(this.getLeftAttribute().getName()).append("<>");
		result.append(this.getRightAlias()==null ? this.getRight().getName():this.getRightAlias()).append(".").append(this.getRightAttribute().getName());
		return result.toString();
	}
	
	/**
	 * @param t
	 * @return true if the tuple t satisfies the predicate
	 * @see uk.ac.ox.cs.pdq.algebra.predicates.Predicate#isSatisfied(Tuple)
	 */
	@Override
	public boolean isSatisfied(Tuple t) {
		return !super.isSatisfied(t);
	}

}
