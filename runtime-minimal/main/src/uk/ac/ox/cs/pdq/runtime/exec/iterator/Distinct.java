package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.runtime.util.RuntimeUtilities;

/**
 * Distinct removes duplicates in a result set.
 * 
 * @author Julien Leblay
 */
public class Distinct extends TupleIterator {
	
	/**  The sole child of the operator. */
	protected final TupleIterator child;

	/**  The iteratorCache. */
	protected Set<Tuple> cacheOfOutputTuples = new LinkedHashSet<>();
	
	/**  The next result to return. */
	protected Tuple nextResult = null;
	
	public Distinct(TupleIterator child) {
		super(child.getInputAttributes(), child.getOutputAttributes());
		Assert.assertNotNull(child);
		this.child = child;
	}
	
	@Override
	public TupleIterator[] getChildren() {
		return new TupleIterator[]{this.child};
	}

	@Override
	public TupleIterator getChild(int childIndex) {
		Assert.assertTrue(childIndex == 0);
		return this.child;
	}

	@Override
	public void open() {
		Assert.assertTrue(this.open == null || this.open);
		this.child.open();
		this.open = true;
		this.nextTuple();
	}
	
	@Override
	public void reset() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		this.child.reset();
		this.cacheOfOutputTuples.clear();
		this.nextTuple();
	}

	@Override
	public void interrupt() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		this.nextResult = null;
		this.interrupted = true;
	}
	
	@Override
	public boolean hasNext() {
		Assert.assertTrue(this.open != null && this.open);
		return !this.interrupted && this.nextResult != null;
	}

	@Override
	public Tuple next() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		if (this.nextResult == null) {
			throw new NoSuchElementException("End of operator reached.");
		}
		Tuple result = this.nextResult;
		this.nextTuple();
		return result;
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
		} while (this.cacheOfOutputTuples.contains(this.nextResult));
		if (this.nextResult != null) {
			this.cacheOfOutputTuples.add(this.nextResult);
		}
	}
	
	@Override
	public void receiveTupleFromParentAndPassItToChildren(Tuple t) {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(t != null);
		Assert.assertTrue(RuntimeUtilities.typeOfAttributesEqualsTupleType(t.getType(), this.inputAttributes));
		this.child.receiveTupleFromParentAndPassItToChildren(t);
	}
}
