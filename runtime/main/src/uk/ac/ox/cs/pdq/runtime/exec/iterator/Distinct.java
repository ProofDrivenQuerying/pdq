package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.datasources.utility.Tuple;

// TODO: Auto-generated Javadoc
/**
 * Distinct removes duplicates in a result set.
 * 
 * @author Julien Leblay
 */
public class Distinct extends UnaryIterator {

	/**  The iteratorCache. */
	private Set<Tuple> cache = new LinkedHashSet<>();
	
	/**  The next result to return. */
	private Tuple nextResult = null;
	
	/**
	 * Instantiates a new operator.
	 * 
	 * @param child TupleIterator
	 */
	public Distinct(TupleIterator child) {
		super(child);
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.UnaryIterator#open()
	 */
	@Override
	public void open() {
		super.open();
		this.nextTuple();
	}

	/**
	 * Prepares the next tuple to return. If the end of the operator is reached,
	 * nextTuple shall be null.
	 */
	private void nextTuple() {
		this.nextResult = null;
		do {
			if (this.child.hasNext()) {
				this.nextResult = this.child.next();
				
			} else {
				this.nextResult = null;
			}
		} while (this.cache.contains(this.nextResult));
		if (this.nextResult != null) {
			this.cache.add(this.nextResult);
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		Preconditions.checkState(this.open != null && this.open);
		return !this.interrupted && this.nextResult != null;
	}

	/**
	 * {@inheritDoc}
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Tuple next() {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		if (this.nextResult == null) {
			throw new NoSuchElementException("End of operator reached.");
		}
		Tuple result = this.nextResult;
		this.nextTuple();
		return result;
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.UnaryIterator#reset()
	 */
	@Override
	public void reset() {
		super.reset();
		this.cache.clear();
		this.nextTuple();
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.UnaryIterator#interrupt()
	 */
	@Override
	public void interrupt() {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		this.nextResult = null;
		this.interrupted = true;
	}
}
