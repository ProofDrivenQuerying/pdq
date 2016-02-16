package uk.ac.ox.cs.pdq.algebra.predicates;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import uk.ac.ox.cs.pdq.util.Tuple;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * Evaluates a Collection of predicates conjunctively, i.e. all the underlying
 * predicates must be satisfied for this predicate to be satisfied.
 *
 * @author Julien Leblay
 * @param <T> the generic type
 */
public class ConjunctivePredicate<T extends Predicate> implements Predicate, Iterable<T> {

	/**  The value to which the tuple must be equals at the given position. */
	private final Collection<T> predicates;

	/**
	 * Default constructor.
	 */
	public ConjunctivePredicate() {
		this.predicates = Lists.<T>newArrayList();
	}

	/**
	 * Default constructor.
	 *
	 * @param predicate T
	 */
	public ConjunctivePredicate(T predicate) {
		this();
		this.addPredicate(predicate);
	}

	/**
	 * Default constructor.
	 *
	 * @param predicates the predicates
	 */
	public ConjunctivePredicate(Collection<T> predicates) {
		this();
		Preconditions.checkArgument(predicates != null);
		for (T p: predicates) {
			this.addPredicate(p);
		}
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

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.plan.relational.logical.predicates.Predicate#isSatisfied(uk.ac.ox.cs.pdq.tuple.Tuple)
	 */
	@Override
	public boolean isSatisfied(Tuple t) {
		for (Predicate p: this.predicates) {
			if (!p.isSatisfied(t)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Flatten.
	 *
	 * @return a collection of predicate remove the nesting of conjunction that
	 * it may contain.
	 */
	public Collection<Predicate> flatten() {
		return this.flatten(this);
	}
	
	/**
	 * Flatten.
	 *
	 * @param predicate the (possibly nested) predicate to flatten, if null the empty collection is returned.
	 * @return a collection of predicate remove the nesting of conjunction that
	 * it may contain.
	 */
	public Collection<Predicate> flatten(Predicate predicate) {
		Collection<Predicate> result = new LinkedList<Predicate>();
		if (predicate != null) {
			if (predicate instanceof ConjunctivePredicate) {
				ConjunctivePredicate<Predicate> conjunction = (ConjunctivePredicate) predicate;
				for (Predicate subPred: conjunction) {
					result.addAll(flatten(subPred));
				}
			} else {
				result.add(predicate);
			}
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
