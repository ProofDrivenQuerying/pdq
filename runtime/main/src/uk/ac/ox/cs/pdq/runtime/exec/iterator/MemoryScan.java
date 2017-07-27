package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * MemoryScan over an in-memory relation or simply a collection of tuples.
 * 
 * @author Julien Leblay
 */
public class MemoryScan extends TupleIterator {

	/** The next tuple to return. */
	protected Iterator<Tuple> tupleIterator = null;

	/** The next tuple to return. */
	protected Iterable<Tuple> data = null;

	/**  The next tuple to return. */
	private Tuple nextTuple;
	
	/** The filter. */
	protected final Condition filter;	

	/**
	 * Instantiates a new memory scan.
	 * @param columns List<Typed>
	 * @param data Iterable<Tuple>
	 * @param filter additional filtering on the scan 
	 */
	public MemoryScan(List<Typed> columns, Iterable<Tuple> data, Condition filter) {
		super(Lists.<Typed>newArrayList(), columns);
		Preconditions.checkArgument(data != null);
		Iterator<Tuple> testIterator = data.iterator();
		Preconditions.checkArgument(!testIterator.hasNext()
				|| TupleType.DefaultFactory.createFromTyped(columns)
						.equals(testIterator.next().getType()));
		this.data = data;
		this.filter = filter;
	}

	/**
	 * Instantiates a new memory scan.
	 * @param columns List<Typed>
	 * @param data Iterable<Tuple>
	 */
	public MemoryScan(List<Typed> columns, Iterable<Tuple> data) {
		this(columns, data, null);
	}
	
	/**
	 * Gets the filter.
	 *
	 * @return the filter for this scan if any
	 */
	public Condition getFilter() {
		return this.filter;
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
		if (this.filter != null) {
			result.append("[#").append(filter).append(']');
		}
		return result.toString();
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.datasources.ResetableIterator#open()
	 */
	@Override
	public void open() {
		Preconditions.checkState(this.open == null);
		this.open = true;
		this.tupleIterator = this.data.iterator();
		this.nextTuple();
	}

	/**
	 * Moves to the next valid tuple to return.
	 */
	private void nextTuple() {
		while (this.tupleIterator.hasNext()) {
			this.nextTuple = this.tupleIterator.next();
			if (this.filter == null || this.filter.isSatisfied(this.nextTuple)) {
				return;
			}
		}
		this.nextTuple = null;
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#close()
	 */
	@Override
	public void close() {
		Preconditions.checkState(this.open != null && this.open);
		super.close();
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
		this.tupleIterator = this.data.iterator();
		this.nextTuple();
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
		if (this.eventBus != null) {
			this.eventBus.post(this);
		}
		if (this.nextTuple == null) {
			throw new NoSuchElementException();
		}
		Tuple result = this.nextTuple;
		this.nextTuple();
		return result;
	}

	/**
	 * Checks for next.
	 *
	 * @return boolean
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		Preconditions.checkState(this.open != null && this.open);
		return !this.interrupted && this.nextTuple != null;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#deepCopy()
	 */
	@Override
	public MemoryScan deepCopy() {
		return new MemoryScan(this.columns, this.data, this.filter);
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
	 * Bind.
	 *
	 * @param t Tuple
	 */
	@Override
	public void bind(Tuple t) {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		Preconditions.checkArgument(t.size() == 0);
		// Important: this iterator MUST be reiterated from scratch
		this.reset(); 
	}
}