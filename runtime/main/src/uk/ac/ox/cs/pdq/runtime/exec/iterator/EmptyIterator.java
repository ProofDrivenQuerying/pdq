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
	}

	@Override
	public void close() {
		Assert.assertTrue(this.open != null && this.open);
		this.open = false;
	}

	@Override
	public void reset() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
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
		Assert.assertTrue(!this.interrupted);
		return false;
	}

	@Override
	public Tuple next() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		throw new NoSuchElementException();
	}

	@Override
	public void receiveTupleFromParentAndPassItToChildren(Tuple tuple) {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		Assert.assertTrue(tuple != null);
		Assert.assertTrue(tuple.size() == 0);
	}
}