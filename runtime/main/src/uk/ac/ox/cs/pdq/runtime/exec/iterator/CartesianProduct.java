package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.datasources.utility.TupleType;
import uk.ac.ox.cs.pdq.runtime.util.RuntimeUtilities;


// TODO: Auto-generated Javadoc
/**
 * CrossProduct implements a n-ary cartesian product in a nested-loop fashion.
 * 
 * @author Julien Leblay
 */
public class CartesianProduct extends TupleIterator {
	
	protected final TupleIterator[] children = new TupleIterator[2]; 

	protected final Integer[] inputPositionsForChild1;

	protected final Integer[] inputPositionsForChild2;
	
	protected final TupleType child1TupleType;
	
	protected final TupleType child2TupleType;
	
	/** A stack of tuple fragment, used in the incremental computation of the cross product. */
	protected Deque<Tuple> tupleStack = new ArrayDeque<>();

	/** Determines whether the operator is known to have an empty result. */
	protected boolean isEmpty = false;

	/** The next tuple to return. */
	protected Tuple nextTuple = null;

	/**
	 * Instantiates a new cross product.
	 *
	 * @param children the children
	 */
	public CartesianProduct(TupleIterator child1, TupleIterator child2) {
		super(RuntimeUtilities.computeInputAttributes(child1, child2), RuntimeUtilities.computeOutputAttributes(child1, child2));
		Assert.assertNotNull(child1);
		Assert.assertNotNull(child2);
		this.children[0] = child1;
		this.children[1] = child2;
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
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.NaryIterator#open()
	 */
	@Override
	public void open() {
		Assert.assertTrue(this.open == null || this.open);
		for (TupleIterator child: this.children) {
			child.open();
		}
		this.open = true;
		if (this.inputAttributes.length == 0) {
			this.nextTuple();
		}
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#close()
	 */
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
	 * @see uk.ac.ox.cs.pdq.util.ResetableIterator#reset()
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
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		if (this.eventBus != null) {
			this.eventBus.post(this);
		}
		Tuple result = this.nextTuple;
		this.nextTuple = null;
		if ((!this.hasNext() && result == null) || this.isEmpty) {
			throw new NoSuchElementException("End of operator reached.");
		}
		return result;
	}
	
	/**
	 * Finds the next tuple to return. If new tuple can be obtain for this 
	 * operator, the nextTuple attribute will be set to null.
	 * 
	 */
	protected void nextTuple() {
		if (this.interrupted) {
			this.nextTuple = null;
			return;
		}
		if (this.nextInCrossProduct(0)) {
			this.nextTuple = Tuple.EmptyTuple;
			for (Iterator<Tuple> it = this.tupleStack.descendingIterator(); it.hasNext();) {
				this.nextTuple = this.nextTuple.appendTuple(it.next());
			}
			this.tupleStack.pop();
		} else {
			this.nextTuple = null;
		}
	}
	
	/**
	 * Computes the next tuple in the cross product.
	 * 
	 * @param i
	 *            the level at which to start the computation
	 * @return true, if the tuple fragment at the given level could be completed.
	 */
	protected boolean nextInCrossProduct(int i) {
		if (i < this.children.length) {
			TupleIterator child = this.children[i];
			if (this.tupleStack.size() > i) {
				if (this.nextInCrossProduct(i + 1)) {
					return true;
				}
			}
			while (child.hasNext()) {
				this.tupleStack.push(child.next());
				if (this.nextInCrossProduct(i + 1)) {
					return true;
				}
			}
			if (i > 0) {
				child.reset();
			}
			if (!this.tupleStack.isEmpty()) {
				this.tupleStack.pop();
			}
			return false;
		}
		return true;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#bind(uk.ac.ox.cs.pdq.util.Tuple)
	 */
	@Override
	public void bind(Tuple tuple) {
		Assert.assertTrue(this.open != null && this.open);
		Assert.assertTrue(!this.interrupted);
		Object[] inputsForLeftChild = RuntimeUtilities.projectValuesInInputPositions(tuple, this.inputPositionsForChild1);
		Object[] inputsForRightChild = RuntimeUtilities.projectValuesInInputPositions(tuple, this.inputPositionsForChild2);
		this.children[0].bind(this.child1TupleType.createTuple(inputsForLeftChild));
		this.children[1].bind(this.child2TupleType.createTuple(inputsForRightChild));
	}
}