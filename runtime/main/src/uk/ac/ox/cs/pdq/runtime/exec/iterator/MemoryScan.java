package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.runtime.util.RuntimeUtilities;

/**
 * MemoryScan over an in-memory relation or simply a collection of tuples.
 * 
 * @author Julien Leblay
 */
public class MemoryScan extends TupleIterator {

	/** The next tuple to return. */
	protected Iterator<Tuple> outputTuplesIterator = null;

	/** The next tuple to return. */
	protected Iterable<Tuple> dataStoredInMemory = null;

	/**  The next tuple to return. */
	protected Tuple nextTuple;

	public MemoryScan(Attribute[] outputAttributes, Iterable<Tuple> data) {
		super(new Attribute[0], outputAttributes);
		Assert.assertTrue(data != null);
		Iterator<Tuple> testIterator = data.iterator();
		Assert.assertTrue(!testIterator.hasNext() || RuntimeUtilities.typeOfAttributesEqualsTupleType(testIterator.next().getType(), this.outputAttributes));
		this.dataStoredInMemory = data;
	}
	
	@Override
	public TupleIterator[] getChildren() {
		return new TupleIterator[]{};
	}

	@Override
	public TupleIterator getChild(int childIndex) {
		return null;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName());
		return result.toString();
	}

	@Override
	public void open() {
		Assert.assertTrue(this.open == null);
		this.open = true;
		this.outputTuplesIterator = this.dataStoredInMemory.iterator();
		this.nextTuple();
	}
	
	@Override
	public void close() {
		Assert.assertTrue(this.open != null && this.open);
		super.close();
	}

	@Override
	public void reset() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		this.outputTuplesIterator = this.dataStoredInMemory.iterator();
		this.nextTuple();
	}
	
	@Override
	public void interrupt() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		this.interrupted = true;
	}
	
	@Override
	public boolean hasNext() {
		Assert.assertTrue(this.open != null && this.open);
		return !this.interrupted && this.nextTuple != null;
	}

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
		while (this.outputTuplesIterator.hasNext()) {
			this.nextTuple = this.outputTuplesIterator.next();
			return;
		}
		this.nextTuple = null;
	}
	
	@Override
	public void receiveTupleFromParentAndPassItToChildren(Tuple t) {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		Assert.assertTrue(t.size() == 0);
		// Important: this iterator MUST be reiterated from scratch
		this.reset(); 
	}
}