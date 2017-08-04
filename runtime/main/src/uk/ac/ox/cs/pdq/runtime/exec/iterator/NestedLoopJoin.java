package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.runtime.util.RuntimeUtilities;


// TODO: Auto-generated Javadoc
/**
 * Nested loop implementation for n-ary joins.
 * 
 * @author Julien Leblay
 */
public class NestedLoopJoin extends Join {

	/**
	 * A stack of tuple fragment, used in the incremental computation of the 
	 * cross product.
	 */
	protected Deque<Tuple> tupleStack = new ArrayDeque<>();

	/** The has next. */
	protected boolean hasNext = true;

	/**
	 * Constructor an unbound array of children.
	 *
	 * @param left TupleIterator
	 * @param right TupleIterator
	 */
	public NestedLoopJoin(TupleIterator child1, TupleIterator child2) {
		super(child1, child2);
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.Join#open()
	 */
	@Override
	public void open() {
		super.open();
		this.hasNext = true;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.NaryIterator#reset()
	 */
	@Override
	public void reset() {
		super.reset();
		this.hasNext = true;
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.Join#hasNext()
	 */
	public boolean hasNext() {
		return hasNext && super.hasNext() && this.nextTuple != null;
	}

	/**
	 * Finds the next tuple to return. If new tuple can be obtain for this 
	 * operator, the nextTuple attribute will be set to null.
	 * 
	 */
	@Override
	protected void nextTuple() {
		if (this.interrupted || !this.hasNext) {
			this.nextTuple = null;
			return;
		}
		do  {
			if (this.nextInCrossProduct(0)) {
				this.nextTuple = Tuple.EmptyTuple;
				for (Iterator<Tuple> it = this.tupleStack.descendingIterator(); it.hasNext();) {
					this.nextTuple = this.nextTuple.appendTuple(it.next());
				}
				this.tupleStack.pop();
			} else {
				this.nextTuple = null;
				this.hasNext = false;
				break;
			}
		} while (!RuntimeUtilities.isSatisfied(this.joinConditions, this.nextTuple));
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
			child.reset();
			if (!this.tupleStack.isEmpty()) {
				this.tupleStack.pop();
			}
			return false;
		}
		return true;
	}
}