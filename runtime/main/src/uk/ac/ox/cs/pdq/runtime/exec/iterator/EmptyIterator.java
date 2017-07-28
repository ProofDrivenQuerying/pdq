package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * The empty iterator is used to model iterator of an empty set.
 * Its hasNext() method always return false, next() throws a 
 * NoSuchElementException and other method have basically no effect.
 * 
 * @author Julien Leblay
 */
public class EmptyIterator extends TupleIterator {

	/**
	 * Instantiates a new memory scan.
	 * 
	 */
	public EmptyIterator() {
		super(new LinkedList<Typed>(), new LinkedList<Typed>());
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName());
		return result.toString();
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.datasources.ResetableIterator#open()
	 */
	@Override
	public void open() {
		Preconditions.checkState(this.open == null);
		this.open = true;
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#close()
	 */
	@Override
	public void close() {
		Preconditions.checkState(this.open != null && this.open);
		this.open = false;
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.datasources.ResetableIterator#reset()
	 */
	@Override
	public void reset() {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
	}

	/**
	 * {@inheritDoc}
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		return false;
	}

	/**
	 * {@inheritDoc}
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Tuple next() {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		throw new NoSuchElementException();
	}

	/**
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
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#bind(uk.ac.ox.cs.pdq.datasources.utility.Tuple)
	 */
	@Override
	public void bind(Tuple t) {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		Preconditions.checkArgument(t != null);
		Preconditions.checkArgument(t.getType().equals(this.inputType));
	}
}