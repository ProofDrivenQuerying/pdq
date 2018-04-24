package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.NoSuchElementException;

import org.junit.Assert;

import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.datasources.utility.TupleType;
import uk.ac.ox.cs.pdq.db.Attribute;

/**
 * Rename operator.
 * 
 * @author Gabor
 */
public class Rename extends TupleIterator {

	protected final TupleIterator child;
	
	protected final Attribute[] renamedAttributes;

	protected final TupleType childTupleType;
	
	protected final TupleType projectionsTupleType;
	
	public Rename(Attribute[] renamedAttributes, TupleIterator child) {
		super(child.getInputAttributes(), renamedAttributes);
		Assert.assertNotNull(renamedAttributes);
		Assert.assertNotNull(child);
		Assert.assertEquals((int)child.getNumberOfOutputAttributes(), renamedAttributes.length);
		this.renamedAttributes = renamedAttributes.clone();
		this.child = child;
		this.childTupleType = TupleType.DefaultFactory.createFromTyped(this.inputAttributes);
		this.projectionsTupleType = TupleType.DefaultFactory.createFromTyped(this.renamedAttributes);
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
	public String[] getColumnsDisplay() {
		String[] result = new String[this.renamedAttributes.length];
		for(int index = 0; index < this.renamedAttributes.length; ++index)
			result[index] = this.renamedAttributes[index].getType().toString();
		return result;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName());
		result.append(this.getColumnsDisplay());
		result.append('(').append(this.child).append(')');
		return result.toString();
	}

	@Override
	public void setEventBus(EventBus eb) {
		super.setEventBus(eb);
		this.child.setEventBus(eb);
	}
	
	@Override
	public void open() {
		Assert.assertTrue(this.open == null || this.open);
		this.child.open();
		this.open = true;
	}

	@Override
	public void close() {
		Assert.assertTrue(this.open != null && this.open);
		super.close();
		this.child.close();
	}

	@Override
	public void reset() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		this.child.reset();
	}


	@Override
	public void interrupt() {
		Assert.assertTrue(this.open != null && this.open);
		this.interrupted = true;
		this.child.interrupt();
	}
	
	@Override
	public boolean hasNext() {
		Assert.assertTrue(this.open != null && this.open);
		return !this.interrupted && this.child.hasNext();
	}
	
	@Override
	public Tuple next() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		if (this.eventBus != null) {
			this.eventBus.post(this);
		}
		Tuple next = this.child.next();
		if (next == null) {
			throw new NoSuchElementException("End of projection operator reached.");
		}
		return next; // no change needed, just the input output signature changes in rename. 
	}

	@Override
	public void receiveTupleFromParentAndPassItToChildren(Tuple tuple) {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(tuple != null);
		Assert.assertTrue(tuple.getType().equals(this.childTupleType));
		this.child.receiveTupleFromParentAndPassItToChildren(tuple);
	}
}
