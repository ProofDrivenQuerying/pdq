package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import org.junit.Assert;

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
	protected final Deque<Tuple> outputTuplesCache = new LinkedList<>();

	/** Inner iterator. */
	protected final TupleIterator child;

	/** True if the iterator has been reset. */
	protected boolean isReset = false;

	protected Iterator<Tuple> outputTuplesIterator;
	
	public LasyCachedIterator(TupleIterator child) {
		super(child.getInputAttributes(), child.getOutputAttributes());
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
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName());
		result.append('(').append(this.child).append(')');
		return result.toString();
	}

	@Override
	public void open() {
		Assert.assertTrue(this.open == null);
		this.isReset = false;
	}

	@Override
	public void reset() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		this.isReset = true;
		this.outputTuplesIterator = this.outputTuplesCache.iterator();
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
		if (this.interrupted) {
			return false;
		}
		if (this.isReset) {
			return this.outputTuplesIterator.hasNext();
		}
		return this.child.hasNext();
	}

	@Override
	public Tuple next() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		if (this.isReset) {
			return this.outputTuplesIterator.next();
		}
		Tuple result = this.child.next();
		this.outputTuplesCache.add(result);
		return result;
	}

	@Override
	public void remove() {
		if (!this.isReset) {
			this.outputTuplesCache.removeLast();
		}
	}

	@Override
	public void receiveTupleFromParentAndPassItToChildren(Tuple tuple) {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		this.child.receiveTupleFromParentAndPassItToChildren(tuple);
	}
}