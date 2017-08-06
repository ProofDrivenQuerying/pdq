package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.runtime.util.RuntimeUtilities;

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
	protected Tuple nextTuple;

	/**
	 * Instantiates a new memory scan.
	 * @param columns List<Typed>
	 * @param data Iterable<Tuple>
	 * @param filter additional filtering on the scan 
	 */
	public MemoryScan(Attribute[] outputAttributes, Iterable<Tuple> data) {
		super(new Attribute[0], outputAttributes);
		Assert.assertTrue(data != null);
		Iterator<Tuple> testIterator = data.iterator();
		Assert.assertTrue(!testIterator.hasNext() || RuntimeUtilities.typeOfAttributesEqualsTupleType(testIterator.next().getType(), this.outputAttributes));
		this.data = data;
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
	 * 
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
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.datasources.ResetableIterator#open()
	 */
	@Override
	public void open() {
		Assert.assertTrue(this.open == null);
		this.open = true;
		this.tupleIterator = this.data.iterator();
		this.nextTuple();
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#close()
	 */
	@Override
	public void close() {
		Assert.assertTrue(this.open != null && this.open);
		super.close();
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.datasources.ResetableIterator#reset()
	 */
	@Override
	public void reset() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		this.tupleIterator = this.data.iterator();
		this.nextTuple();
	}
	
	/**
	 * 
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
	 * Checks for next.
	 *
	 * @return boolean
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		Assert.assertTrue(this.open != null && this.open);
		return !this.interrupted && this.nextTuple != null;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Tuple next() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
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
	 * Moves to the next valid tuple to return.
	 */
	private void nextTuple() {
		while (this.tupleIterator.hasNext()) {
			this.nextTuple = this.tupleIterator.next();
			return;
		}
		this.nextTuple = null;
	}
	
	/**
	 * Bind.
	 *
	 * @param t Tuple
	 */
	@Override
	public void receiveTupleFromParentAndPassItToChildren(Tuple t) {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		Assert.assertTrue(t.size() == 0);
		// Important: this iterator MUST be reiterated from scratch
		this.reset(); 
	}
}