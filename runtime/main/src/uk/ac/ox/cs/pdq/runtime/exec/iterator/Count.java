package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Count counts the number of tuples in a result set.
 * 
 * @author Julien Leblay
 */
public class Count extends UnaryIterator {

	/** The current result's count. */
	private Integer nextResult = null;
	
	/**
	 * Instantiates a new operator.
	 * 
	 * @param child TupleIterator
	 */
	public Count(TupleIterator child) {
		super(Lists.<Typed>newArrayList(
				new Attribute(Integer.class, Count.class.getSimpleName())),
				child);
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#deepCopy()
	 */
	@Override
	public Count deepCopy() {
		return new Count(this.child.deepCopy());
	}

	/**
	 * {@inheritDoc}
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		Preconditions.checkState(this.open != null && this.open);
		return !this.interrupted && this.nextResult == null;
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
			this.nextResult = 0;
			while (this.child.hasNext()) {
				this.nextResult++;
				this.child.next();
			}
			return this.getType().createTuple(this.nextResult);
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
		this.nextResult = null;
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#getColumnsDisplay()
	 */
	@Override
	public List<String> getColumnsDisplay() {
		List<String> result = new ArrayList<>();
		StringBuilder builder = new StringBuilder("COUNT(");
		String sep = "";
		for (String s: super.getColumnsDisplay()) {
			builder.append(sep).append(s);
			sep = ",";
		}
		builder.append(')');
		result.add(builder.toString());
		return result;
	}
}
