package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.NoSuchElementException;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.db.Attribute;

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
		super(new Attribute[]{}, new Attribute[]{});
	}
	
	@Override
	public TupleIterator[] getChildren() {
		return new TupleIterator[]{};
	}

	@Override
	public TupleIterator getChild(int childIndex) {
		return null;
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
		Assert.assertTrue(this.open == null);
		this.open = true;
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#close()
	 */
	@Override
	public void close() {
		Assert.assertTrue(this.open != null && this.open);
		this.open = false;
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.datasources.ResetableIterator#reset()
	 */
	@Override
	public void reset() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
	}
	
	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#interrupt()
	 */
	@Override
	public void interrupt() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		this.interrupted = true;
	}

	/**
	 * {@inheritDoc}
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		return false;
	}

	/**
	 * {@inheritDoc}
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Tuple next() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		throw new NoSuchElementException();
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#bind(uk.ac.ox.cs.pdq.datasources.utility.Tuple)
	 */
	@Override
	public void bind(Tuple tuple) {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		Assert.assertTrue(tuple != null);
		Assert.assertTrue(tuple.size() == 0);
	}
}