package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import uk.ac.ox.cs.pdq.util.Tuple;

import com.google.common.base.Preconditions;

/**
 * An iterator that wraps another iterator. Caches and return tuples from the
 * underlying iterator upon the first traversal. Any subsequent traversal, 
 * occurring after a reset of performed on the cached items. 
 * 
 * @author Julien Leblay
 */
public class LasyCachedIterator extends TupleIterator {

	/** Cached items. */
	protected final Deque<Tuple> cache = new LinkedList<>();

	/** Inner iterator. */
	protected final TupleIterator inner;

	/** True if the iterator has been reset. */
	protected boolean isReset = false;

	/** Cached items. */
	protected Iterator<Tuple> iterator;
	
	/**
	 * Instantiates a new join.
	 * 
	 * @param inner TupleIterator
	 */
	public LasyCachedIterator(TupleIterator inner) {
		super(inner.getInputColumns(), inner.getColumns());
		this.inner = inner;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName());
		result.append('(').append(this.inner).append(')');
		return result.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.UnaryIterator#open()
	 */
	@Override
	public void open() {
		Preconditions.checkState(this.open == null);
		this.isReset = false;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.UnaryIterator#reset()
	 */
	@Override
	public void reset() {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		this.isReset = true;
		this.iterator = this.cache.iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.UnaryIterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		Preconditions.checkState(this.open != null && this.open);
		if (this.interrupted) {
			return false;
		}
		if (this.isReset) {
			return this.iterator.hasNext();
		}
		return this.inner.hasNext();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#next()
	 */
	@Override
	public Tuple next() {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		if (this.isReset) {
			return this.iterator.next();
		}
		Tuple result = this.inner.next();
		this.cache.add(result);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#remove()
	 */
	@Override
	public void remove() {
		if (!this.isReset) {
			this.cache.removeLast();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#deepCopy()
	 */
	@Override
	public LasyCachedIterator deepCopy() {
		return new LasyCachedIterator(this.inner);
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#interrupt()
	 */
	@Override
	public void interrupt() {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		this.interrupted = true;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#bind(uk.ac.ox.cs.pdq.util.Tuple)
	 */
	@Override
	public void bind(Tuple t) {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		this.inner.bind(t);
	}
}