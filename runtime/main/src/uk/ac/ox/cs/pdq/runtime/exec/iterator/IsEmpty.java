package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.NoSuchElementException;

import org.junit.Assert;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.datasources.utility.TupleType;
import uk.ac.ox.cs.pdq.db.Attribute;

// TODO: Auto-generated Javadoc
/**
 * IsEmpty of the boolean query operator. Return true if its child return any
 * tuple, false otherwise.
 * 
 * @author Julien Leblay
 */
public class IsEmpty extends TupleIterator {
	
	/**  The sole child of the operator. */
	protected final TupleIterator child;
	
	protected final TupleType childTupleType;
	
	protected final TupleType outputTupleType;

	/** The next tuple in the iterator. */
	private Tuple nextTuple = null;
	
	/**
	 * Instantiates a new operator.
	 * 
	 * @param child TupleIterator
	 */
	public IsEmpty(TupleIterator child) {
		super(child.getInputAttributes(), new Attribute[]{Attribute.create(Boolean.class, IsEmpty.class.getSimpleName())});
		this.child = child;
		this.childTupleType = TupleType.DefaultFactory.createFromTyped(this.inputAttributes);
		this.outputTupleType = TupleType.DefaultFactory.createFromTyped(this.outputAttributes);
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

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#getColumnsDisplay()
	 */
	@Override
	public String[] getColumnsDisplay() {
		return new String[]{"IS_EMPTY"};
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
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.UnaryIterator#reset()
	 */
	@Override
	public void reset() {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		this.child.reset();
		this.nextTuple = null;
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
	 * {@inheritDoc}
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		Preconditions.checkState(this.open != null && this.open);
		return !this.interrupted && this.nextTuple == null;
	}

	/**
	 * {@inheritDoc}
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Tuple next() {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		if (this.nextTuple == null) {
			this.nextTuple = this.outputTupleType.createTuple(new Object[]{!this.child.hasNext()});
			return nextTuple;
		}
		throw new NoSuchElementException("End of operator reached.");
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