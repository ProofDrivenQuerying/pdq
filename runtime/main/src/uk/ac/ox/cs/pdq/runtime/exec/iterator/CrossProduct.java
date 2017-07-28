package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.datasources.utility.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;


// TODO: Auto-generated Javadoc
/**
 * CrossProduct implements a n-ary cartesian product in a nested-loop fashion.
 * 
 * @author Julien Leblay
 */
public class CrossProduct extends NaryIterator {

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
	public CrossProduct(TupleIterator... children) {
		this(toList(children));
	}

	/**
	 * Instantiates a new cross product.
	 *
	 * @param children the children
	 */
	public CrossProduct(List<TupleIterator> children) {
		this(inferInputColumns(children), children);
	}

	/**
	 * Instantiates a new cross product.
	 *
	 * @param inputs List<Typed>
	 * @param children            the children
	 */
	public CrossProduct(List<Typed> inputs, List<TupleIterator> children) {
		super(TupleType.DefaultFactory.createFromTyped(inputs), inputs,
				inferType(children), inferColumns(children), children);
		this.relativeInputPositions = ImmutableMap.copyOf(inferInputMappings(inputs, children));
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.NaryIterator#open()
	 */
	@Override
	public void open() {
		super.open();
		if (this.inputType == TupleType.EmptyTupleType) {
			this.nextTuple();
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
	 * {@inheritDoc}
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		Preconditions.checkState(this.open != null && this.open);
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
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
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
}