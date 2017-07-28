package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * IsEmpty of the boolean query operator. Return true if its child return any
 * tuple, false otherwise.
 * 
 * @author Julien Leblay
 */
public class IsEmpty extends UnaryIterator {

	/** The next tuple in the iterator. */
	private Tuple nextTuple = null;
	
	/**
	 * Instantiates a new operator.
	 * 
	 * @param child TupleIterator
	 */
	public IsEmpty(TupleIterator child) {
		super(Lists.<Typed>newArrayList(
				Attribute.create(Boolean.class, IsEmpty.class.getSimpleName())),
				child);
	}

	/**
	 * {@inheritDoc}
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		Preconditions.checkState(this.open != null && this.open);
		return !this.interrupted && this.nextTuple == null;
	}

	/**
	 * {@inheritDoc}
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Tuple next() {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		if (this.nextTuple == null) {
			this.nextTuple = this.getType().createTuple(!this.child.hasNext());
			return nextTuple;
		}
		throw new NoSuchElementException("End of operator reached.");
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.UnaryIterator#reset()
	 */
	@Override
	public void reset() {
		super.reset();
		this.nextTuple = null;
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#getColumnsDisplay()
	 */
	@Override
	public List<String> getColumnsDisplay() {
		List<String> result = new ArrayList<>();
		result.add("IS_EMPTY");
		return result;
	}
}
