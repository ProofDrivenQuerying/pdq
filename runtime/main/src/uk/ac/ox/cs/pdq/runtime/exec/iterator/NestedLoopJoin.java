package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.Typed;


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
	 * Instantiates a new nested loop multi-join.
	 * 
	 * @param children
	 *            the children
	 */
	public NestedLoopJoin(TupleIterator... children) {
		this(inferNaturalJoin(toList(children)),
				inferInputColumns(toList(children)), toList(children));
	}

	/**
	 * Instantiates a new nested loop multi-join.
	 * 
	 * @param predicate the join predicate
	 * @param children the children
	 */
	public NestedLoopJoin(Condition predicate, TupleIterator... children) {
		this(predicate, inferInputColumns(toList(children)), toList(children));
	}

	/**
	 * Instantiates a new nested loop multi-join.
	 * 
	 * @param inputs List<Typed>
	 * @param children the children
	 */
	public NestedLoopJoin(List<Typed> inputs, TupleIterator... children) {
		this(inferNaturalJoin(toList(children)), inputs, toList(children));
	}

	/**
	 * Instantiates a new nested loop multi-join.
	 *
	 * @param inputs List<Typed>
	 * @param children            the children
	 */
	public NestedLoopJoin(List<Typed> inputs, List<TupleIterator> children) {
		this(inferNaturalJoin(children), inputs, children);
	}

	/**
	 * Instantiates a new nested loop multi-join.
	 * @param predicate the join predicate
	 * @param inputs List<Typed>
	 * @param children the children
	 */
	public NestedLoopJoin(Condition predicate, List<Typed> inputs, 
			List<TupleIterator> children) {
		super(predicate, inputs, children);
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
	 * Computes the next tuple in the cross product.
	 * 
	 * @param i
	 *            the level at which to start the computation
	 * @return true, if the tuple fragment at the given level could be completed.
	 */
	protected boolean nextInCrossProduct(int i) {
		if (i < this.children.size()) {
			TupleIterator child = this.children.get(i);
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
		} while (!this.predicate.isSatisfied(this.nextTuple));
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#deepCopy()
	 */
	@Override
	public NestedLoopJoin deepCopy() {
		List<TupleIterator> clones = new ArrayList<>();
		for (TupleIterator child: this.children) {
			clones.add(child.deepCopy());
		}
		return new NestedLoopJoin(this.getInputColumns(), clones);
	}
}