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
import uk.ac.ox.cs.pdq.db.TypedConstant;

// TODO: Auto-generated Javadoc
/**
 * Projection operator.
 * 
 * @author Julien Leblay
 */
public class Projection extends TupleIterator {

	/**  The sole child of the operator. */
	protected final TupleIterator child;
	
	protected final Attribute[] projections;

	/** Maps each variable is the head to a position in the children. */
	protected final Map<Attribute, Integer> positionsOfProjectedAttributes;
	
	protected final TupleType childTupleType;
	
	protected final TupleType projectionsTupleType;
	
	public Projection(Attribute[] projections, TupleIterator child) {
		super(child.getInputAttributes(), child.getOutputAttributes());
		Assert.assertNotNull(projections);
		Assert.assertNotNull(child);
		this.positionsOfProjectedAttributes = new LinkedHashMap<>();
		for(int outputAttributeIndex = 0; outputAttributeIndex < projections.length; ++outputAttributeIndex) { 
			int position = Arrays.asList(child.getOutputAttributes()).indexOf(projections[outputAttributeIndex]);
			Assert.assertTrue(position >= 0);
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

	/**
	 * 
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName());
		result.append(this.getColumnsDisplay());
		result.append('(').append(this.child).append(')');
		return result.toString();
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#setEventBus(com.google.common.eventbus.EventBus)
	 */
	@Override
	public void setEventBus(EventBus eb) {
		super.setEventBus(eb);
		this.child.setEventBus(eb);
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.datasources.ResetableIterator#open()
	 */
	@Override
	public void open() {
		Assert.assertTrue(this.open == null || this.open);
		this.child.open();
		this.open = true;
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
		this.child.close();
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
		this.child.reset();
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#interrupt()
	 */
	@Override
	public void interrupt() {
		Assert.assertTrue(this.open != null && this.open);
		this.interrupted = true;
		this.child.interrupt();
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		Assert.assertTrue(this.open != null && this.open);
		return !this.interrupted && this.child.hasNext();
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
		Tuple next = this.child.next();
		if (next == null) {
			throw new NoSuchElementException("End of projection operator reached.");
		}
		Object[] result = new Object[this.projections.length];
		for(int index = 0; index < this.projections.length; ++index) {
			if (this.projections[index].getType() instanceof Attribute) 
				result[index] = next.getValue(this.positionsOfProjectedAttributes.get(this.projections[index].getType()));
			else if (this.projections[index].getType() instanceof TypedConstant) 
				result[index] = ((TypedConstant) this.projections[index].getType()).getValue();
		}
		return this.projectionsTupleType.createTuple(result);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#receiveTupleFromParentAndPassItToChildren(uk.ac.ox.cs.pdq.datasources.utility.Tuple)
	 */
	@Override
	public void receiveTupleFromParentAndPassItToChildren(Tuple tuple) {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(tuple != null);
		Assert.assertTrue(tuple.getType().equals(this.childTupleType));
		this.child.receiveTupleFromParentAndPassItToChildren(tuple);
	}
}
