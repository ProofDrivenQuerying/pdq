package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.Arrays;
import java.util.NoSuchElementException;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.datasources.utility.TupleType;
import uk.ac.ox.cs.pdq.runtime.util.RuntimeUtilities;


// TODO: Auto-generated Javadoc
/**
 * Join is a top-level class for all join implementations.
 * 
 * @author Julien Leblay
 */
public abstract class Join extends TupleIterator {

	/** The children. */
	protected final TupleIterator[] children = new TupleIterator[2];

	protected final Integer[] inputPositionsForChild1;

	protected final Integer[] inputPositionsForChild2;

	/** The predicate. */
	protected final Condition joinConditions;
	
	protected final TupleType child1TupleType;
	
	protected final TupleType child2TupleType;
	
	protected final TupleType outputTupleType;

	/** Determines whether the operator is known to have an empty result. */
	protected boolean isEmpty = false;

	/** The next tuple to return. */
	protected Tuple nextTuple = null;

	/**
	 * Instantiates a new join.
	 * @param predicate Atom
	 * @param inputs List<Typed>
	 * @param children
	 *            the children
	 */
	protected Join(TupleIterator child1, TupleIterator child2) {
		super(RuntimeUtilities.computeInputAttributes(child1, child2), 
				RuntimeUtilities.computeOutputAttributes(child1, child2));
		Assert.assertNotNull(child1);
		Assert.assertNotNull(child2);
		this.children[0] = child1;
		this.children[1] = child2;
		this.joinConditions = RuntimeUtilities.computeJoinConditions(this.children);
		this.inputPositionsForChild1 = new Integer[child1.getNumberOfInputAttributes()];
		this.inputPositionsForChild2 = new Integer[child2.getNumberOfInputAttributes()];
		int index = 0;
		for(int inputAttributeIndex = 0; inputAttributeIndex < child1.getNumberOfInputAttributes(); ++inputAttributeIndex) { 
			int position = Arrays.asList(child1.getOutputAttributes()).indexOf(child1.getInputAttribute(inputAttributeIndex));
			Assert.assertTrue(position >= 0);
			this.inputPositionsForChild1[index++] = position;
		}
		index = 0;
		for(int inputAttributeIndex = 0; inputAttributeIndex < child2.getNumberOfInputAttributes(); ++inputAttributeIndex) { 
			int position = Arrays.asList(child2.getOutputAttributes()).indexOf(child2.getInputAttribute(inputAttributeIndex));
			Assert.assertTrue(position >= 0);
			this.inputPositionsForChild2[index++] = position;
		}
		this.child1TupleType = TupleType.DefaultFactory.createFromTyped(this.children[0].getInputAttributes());
		this.child2TupleType = TupleType.DefaultFactory.createFromTyped(this.children[1].getInputAttributes());
		this.outputTupleType = TupleType.DefaultFactory.createFromTyped(this.outputAttributes);
	}
	
	@Override
	public TupleIterator[] getChildren() {
		return this.children.clone();
	}

	@Override
	public TupleIterator getChild(int childIndex) {
		Assert.assertTrue(childIndex < 2 && childIndex >= 0);
		return this.children[childIndex];
	}

	/**
	 * Gets the predicate.
	 *
	 * @return the join predicate
	 */
	public Condition getJoinConditions() {
		return this.joinConditions;
	}
	
	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.NaryIterator#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName());
		result.append(this.joinConditions).append('(');
		if (this.children != null) {
			for (TupleIterator child: this.children) {
				result.append(child.toString()).append(',');
			}
			result.deleteCharAt(result.length() - 1);
		}
		result.append(')');
		return result.toString();
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.NaryIterator#open()
	 */
	@Override
	public void open() {
		Assert.assertTrue(this.open == null || this.open);
		this.children[0].open();
		this.children[1].open();
		this.open = true;
		if (this.inputAttributes.length == 0) {
			this.nextTuple();
		}
	}

	@Override
	public void close() {
		super.close();
		for (TupleIterator child: this.children) {
			child.close();
		}
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
		for (TupleIterator child: this.children) {
			child.reset();
		}
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
		for (TupleIterator child: this.children) {
			child.interrupt();
		}
	}

	/**
	 * {@inheritDoc}
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		Assert.assertTrue(this.open != null && this.open);
		if (this.interrupted) {
			return false;
		}
		if (this.nextTuple != null) {
			return true;
		}
		if (this.isEmpty) {
			return false;
		}
		this.nextTuple();
		return this.nextTuple != null;
	}

	/**
	 * {@inheritDoc}
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Tuple next() {
		if (this.eventBus != null) {
			this.eventBus.post(this);
		}
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		Tuple result = this.nextTuple;
		this.nextTuple = null;
		if ((!this.hasNext() && result == null) || this.isEmpty) {
			throw new NoSuchElementException("End of operator reached.");
		}
		return result;
	}
	
	/**
	 * Move the iterator forward and prepares the next tuple to be returned.
	 */
	protected abstract void nextTuple();


	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.NaryIterator#receiveTupleFromParentAndPassItToChildren(uk.ac.ox.cs.pdq.datasources.utility.Tuple)
	 */
	@Override
	public void receiveTupleFromParentAndPassItToChildren(Tuple tuple) {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		Object[] inputsForLeftChild = RuntimeUtilities.projectValuesInInputPositions(tuple, this.inputPositionsForChild1);
		Object[] inputsForRightChild = RuntimeUtilities.projectValuesInInputPositions(tuple, this.inputPositionsForChild2);
		this.children[0].receiveTupleFromParentAndPassItToChildren(this.child1TupleType.createTuple(inputsForLeftChild));
		this.children[1].receiveTupleFromParentAndPassItToChildren(this.child2TupleType.createTuple(inputsForRightChild));
		this.nextTuple();
	}

}