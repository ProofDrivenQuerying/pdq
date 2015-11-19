package uk.ac.ox.cs.pdq.algebra.predicates;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.util.Tuple;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;


/**
 * A set equality predicate that keeps the relations of the referenced attributes
 * @author Efthymia Tsamoura
 *
 */
public class ExtendedSetEqualityPredicate implements EqualityPredicate{

	private static Logger log = Logger.getLogger(ExtendedSetEqualityPredicate.class);

	/** The position on which the predicate is evaluated */
	private final int position;

	/** The value to which the tuple must be equals at the given position */
	private final List<Object> values;
	
	private final Relation relation;
	
	private final String alias;
	
	public ExtendedSetEqualityPredicate(int position, List<Object> values, Relation relation, String alias) {
		Preconditions.checkNotNull(relation);
		Preconditions.checkArgument(position >= 0 && position < relation.getArity());
		Preconditions.checkNotNull(values);
		this.position = position;
		this.relation = relation;
		this.values = values;
		this.alias = alias;
	}

	public Attribute getAttribute() {
		return this.relation.getAttribute(this.getPosition());
	}
	
	public Relation getRelation() {
		return this.relation;
	}
	
	public int getPosition() {
		return this.position;
	}
	
	public List<Object> getValues() {
		return this.values;
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
				&& this.getPosition() == ((ExtendedSetEqualityPredicate) o).getPosition()
				&& this.getValues() == ((ExtendedSetEqualityPredicate) o).getValues()
				&& this.getRelation() == ((ExtendedSetEqualityPredicate) o).getRelation();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(this.getPosition(), this.getValues(), this.getRelation());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String set = "";
		int v = 0;
		for(v = 0; v < this.values.size()-1; ++v) {
			if(this.values.get(v) instanceof Number) {
				set += this.values.get(v) + ",";
			}
			else {
				set += "\"" + this.values.get(v) + "\"" + ",";
			}
		}
		
		if(this.values.get(v) instanceof Number) {
			set += this.values.get(v);
		}
		else {
			set += "\"" + this.values.get(v) + "\"";
		}
		
		result.append(this.alias==null ? this.relation.getName():this.alias).append(".").append(this.getAttribute().getName()).
		append(" IN ").append("(").append(set).append(")");
		return result.toString();
	}

	@Override
	public boolean isSatisfied(Tuple t) {
		throw new java.lang.UnsupportedOperationException();
	}
}
