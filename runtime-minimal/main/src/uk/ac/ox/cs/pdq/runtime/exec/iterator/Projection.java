package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Assert;

import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.datasources.utility.TupleType;
import uk.ac.ox.cs.pdq.db.Attribute;

/**
 * Projection operator.
 * 
 * @author Julien Leblay
 */
public class Projection extends TupleIterator {

	protected final TupleIterator child;
	
	protected final Attribute[] projections;

	/** Maps each variable is the head to a position in the children. */
	protected final Map<Attribute, Integer> positionsOfProjectedAttributes;
	
	protected final TupleType childTupleType;
	
	protected final TupleType projectionsTupleType;
	
	public Projection(Attribute[] projections, TupleIterator child) {
		super(child.getInputAttributes(), projections);
		Assert.assertNotNull(projections);
		Assert.assertNotNull(child);
		this.positionsOfProjectedAttributes = new LinkedHashMap<>();
		for(int outputAttributeIndex = 0; outputAttributeIndex < projections.length; ++outputAttributeIndex) { 
			int position = Arrays.asList(child.getOutputAttributes()).indexOf(projections[outputAttributeIndex]);
			if (position == -1)
				throw new IllegalArgumentException("Inconsistent attributes");
			this.positionsOfProjectedAttributes.put(projections[outputAttributeIndex], position);
		}
		this.projections = projections.clone();
		this.child = child;
		this.childTupleType = TupleType.DefaultFactory.createFromTyped(this.inputAttributes);
		this.projectionsTupleType = TupleType.DefaultFactory.createFromTyped(this.projections);
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
		String[] result = new String[this.projections.length];
		for(int index = 0; index < this.projections.length; ++index)
			result[index] = this.projections[index].getType().toString();
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
		Object[] result = new Object[this.projections.length];
		for(int attributeIndex = 0; attributeIndex < this.projections.length; ++attributeIndex) 
			result[attributeIndex] = next.getValue(this.positionsOfProjectedAttributes.get(this.projections[attributeIndex]));
		return this.projectionsTupleType.createTuple(result);
	}

	@Override
	public void receiveTupleFromParentAndPassItToChildren(Tuple tuple) {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(tuple != null);
		Assert.assertTrue(tuple.getType().equals(this.childTupleType));
		this.child.receiveTupleFromParentAndPassItToChildren(tuple);
	}
}
