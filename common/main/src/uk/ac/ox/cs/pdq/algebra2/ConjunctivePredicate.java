package uk.ac.ox.cs.pdq.algebra2;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * TOCOMMENT This seems to be a "conjunction of predicates" object (not a conjunctive predicate)
 * Evaluates a Collection of predicates conjunctively, i.e. all the underlying
 * predicates must be satisfied for this predicate to be satisfied.
 *
 * @author Julien Leblay
 * @param <T> the generic type
 */
public class ConjunctivePredicate<T extends Predicate> implements Predicate, Iterable<T> {

	/** TOCOMMENT  The value to which the tuple must be equals at the given position. */
	private final Collection<T> predicates;
	
	private final Collection<EqualityPredicate> equalityPredicates;

	/**
	 * Default constructor.
	 *
	 * @param predicates the predicates
	 */
	public ConjunctivePredicate(Collection<T> predicates) {
		Preconditions.checkArgument(predicates != null);
		this.predicates = Lists.newArrayList();
		for (T p: predicates) {
			this.addPredicate(p);
		}
		this.equalityPredicates = getEqualityPredicates(this);
	}

	/**
	 * Size.
	 *
	 * @return the number of predicates in the conjunction
	 */
	public int size() {
		return this.predicates.size();
	}

	/**
	 * Checks if is empty.
	 *
	 * @return true if the predicate is empty
	 */
	public boolean isEmpty() {
		return this.predicates.size() == 0;
	}

	/**
	 * Adds the given predicate to the conjunction.
	 *
	 * @param p the p
	 */
	public void addPredicate(T p) {
		Preconditions.checkArgument(p != null);
		this.predicates.add(p);
	}

	/**
	 *
	 * @param predicate the (possibly nested) predicate to flatten, if null the empty collection is returned.
	 * @return a collection of predicates, removing the nesting that it may contain.
	 */
	@Override
	public Collection<EqualityPredicate> getEqualityPredicates() {
		return this.equalityPredicates;
	}
	
	
	public Collection<EqualityPredicate> getEqualityPredicates(Predicate predicate) {
		Preconditions.checkArgument(predicate != null);
		Collection<EqualityPredicate> result = new LinkedHashSet<EqualityPredicate>();
		if (predicate instanceof ConjunctivePredicate) {
			ConjunctivePredicate<Predicate> conjunction = (ConjunctivePredicate) predicate;
			for (Predicate subPred: conjunction) {
				result.addAll(getEqualityPredicates(subPred));
			}
		} else if(predicate instanceof EqualityPredicate){
			result.add((EqualityPredicate)predicate);
		}
		else {
			throw new java.lang.RuntimeException("Unknown predicate type");
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		return this.predicates.iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String sep = "(";
		if (!this.predicates.isEmpty()) {
			for (Predicate p: this) {
				result.append(sep).append(p);
				sep = "&";
			}
			result.append(')');
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
				&& this.predicates.equals(((ConjunctivePredicate) o).predicates);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(this.predicates);
	}
}
