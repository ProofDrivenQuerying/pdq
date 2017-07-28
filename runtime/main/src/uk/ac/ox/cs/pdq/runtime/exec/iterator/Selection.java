package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.NoSuchElementException;

import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.runtime.util.RuntimeUtilities;
import uk.ac.ox.cs.pdq.util.Tuple;

import com.google.common.base.Preconditions;


// TODO: Auto-generated Javadoc
/**
 * Selection operator.
 * 
 * @author Julien Leblay
 */
public class Selection extends UnaryIterator {

	/** The predicate associated with this selection. */
	private final Condition predicate;

	/**  The next Tuple to return. */
	private Tuple nextTuple = null;

	/**
	 * Instantiates a new selection.
	 * @param p Atom
	 * @param child TupleIterator
	 */
	public Selection(Condition p, TupleIterator child) {
		super(child);
		Preconditions.checkArgument(p != null);
		this.predicate = p;
	}

	/**
	 * Prepares the next tuple to be returned. If the end of the iterator
	 * was reached, this.nextTuple is null.
	 */
	private void nextTuple() {
		while (this.child.hasNext()) {
			Tuple next = this.child.next();
			if (RuntimeUtilities.isSatisfied(this.predicate, next)) {
				this.nextTuple = next;
				return;
			}
		}
		this.nextTuple = null;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		Preconditions.checkState(this.open != null && this.open);
		if (this.interrupted) {
			return false;
		}
		if (this.nextTuple != null) {
			return true;
		}
		this.nextTuple();
		return this.nextTuple != null;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Tuple next() {
		if (this.eventBus != null) {
			this.eventBus.post(this);
		}
		this.hasNext();
		if (this.nextTuple == null) {
			throw new NoSuchElementException();
		}
		Tuple result = this.nextTuple;
		this.nextTuple();
		return result;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#deepCopy()
	 */
	@Override
	public Selection deepCopy() {
		return new Selection(this.predicate, this.child.deepCopy());
	}

	/**
	 * Gets the predicate.
	 *
	 * @return Atom
	 */
	public Condition getPredicate() {
		return this.predicate;
	}

	/**
	 * 
	 * {@inheritDoc}
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
}
