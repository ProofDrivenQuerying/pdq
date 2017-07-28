package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.datasources.utility.Tuple;

// TODO: Auto-generated Javadoc
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

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName());
		result.append('(').append(this.inner).append(')');
		return result.toString();
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.datasources.ResetableIterator#open()
	 */
	@Override
	public void open() {
		Preconditions.checkState(this.open == null);
		this.isReset = false;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.datasources.ResetableIterator#reset()
	 */
	@Override
	public void reset() {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		this.isReset = true;
		this.iterator = this.cache.iterator();
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
		if (this.isReset) {
			return this.iterator.hasNext();
		}
		return this.inner.hasNext();
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see java.util.Iterator#next()
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

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#remove()
	 */
	@Override
	public void remove() {
		if (!this.isReset) {
			this.cache.removeLast();
		}
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#interrupt()
	 */
	@Override
	public void interrupt() {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		this.interrupted = true;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#bind(uk.ac.ox.cs.pdq.datasources.utility.Tuple)
	 */
	@Override
	public void bind(Tuple t) {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		this.inner.bind(t);
	}
}