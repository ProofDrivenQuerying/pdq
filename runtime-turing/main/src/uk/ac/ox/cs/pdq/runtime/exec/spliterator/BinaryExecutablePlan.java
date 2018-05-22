package uk.ac.ox.cs.pdq.runtime.exec.spliterator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.IntStream;

import jersey.repackaged.com.google.common.base.Preconditions;
import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.Plan;
import uk.ac.ox.cs.pdq.runtime.exec.PlanDecorator;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;

/**
 * Base class for executable plans having two children. 
 * 
 * @author Tim Hobson
 *
 */
public abstract class BinaryExecutablePlan extends ExecutablePlan {

	protected ExecutablePlan leftChild;
	protected ExecutablePlan rightChild;
	
	public BinaryExecutablePlan(Plan plan, PlanDecorator decorator) throws Exception {
		super(plan,decorator);
		// Check compatibility with the given Plan instance.
		Preconditions.checkArgument(plan.getChildren().length == 2);

		// Assign the child fields to those of the (decorated) child plans.
		this.leftChild = decorator.decorate(this.getDecoratedPlan().getChildren()[0]);
		this.rightChild = decorator.decorate(this.getDecoratedPlan().getChildren()[1]);
	}

	// Base setInputTuples method appropriately sets the fields on the children.
	@Override
	public void setInputTuples(Iterator<Tuple> inputTuples) {
		
		// If either of the child plans requires no dynamic input, simply set 
		// the input tuples on the other child.
		if (leftChild.getInputAttributes().length != 0 && rightChild.getInputAttributes().length == 0) {
			leftChild.setInputTuples(inputTuples);
			return;
		}
		if (leftChild.getInputAttributes().length == 0 && rightChild.getInputAttributes().length != 0) {
			rightChild.setInputTuples(inputTuples);
			return;
		}

		// If both child plans require dynamic input, use the SplitInputsIterator
		// inner class to handle splitting of the tuples and to ensure that (the 
		// appropriate part of) every input tuple is passed to both children.
		Queue<Tuple> leftQueue = new LinkedList<Tuple>();
		Queue<Tuple> rightQueue = new LinkedList<Tuple>();
		leftChild.setInputTuples(new SplitInputsIterator(inputTuples, leftQueue, rightQueue, true));
		rightChild.setInputTuples(new SplitInputsIterator(inputTuples, rightQueue, leftQueue, false));
	}

	public Condition getJoinCondition() {
		return ((JoinTerm) this.getDecoratedPlan()).getJoinConditions();
	}

	@Override
	public void close() {
		this.leftChild.close();
		this.rightChild.close();
	}

	/**
	 * 
	 * Iterator inner class to split the input tuples between the two children. 
	 * Queues are used to ensure that every input tuple is passed to both
	 * children. 
	 */
	private class SplitInputsIterator implements Iterator<Tuple> {

		private Iterator<Tuple> inputTuples;
		Queue<Tuple> queue;
		Queue<Tuple> otherQueue;
		private final Function<Tuple, Tuple> tupleSplitter;

		SplitInputsIterator(Iterator<Tuple> inputTuples, 
				Queue<Tuple> queue, Queue<Tuple> otherQueue, boolean isLeft) {
			
			this.inputTuples = inputTuples;
			this.queue = queue;
			this.otherQueue = otherQueue;

			// Assign the tuple-splitter function. If this is the iterator for the left child
			// (i.e. isLeft is true) this function will extract the first part of the tuple
			// (i.e. the first n values where n = leftChild.getInputAttributes().length). 
			// Otherwise it will extract the second part of the tuple. (Note that we don't use
			// the static tupleProjector method here as the left and right children may
			// contain common input attributes.)
			int startInclusive = isLeft ? 0 : leftChild.getInputAttributes().length;
			int endExclusive = isLeft ? leftChild.getInputAttributes().length: 
				leftChild.getInputAttributes().length + rightChild.getInputAttributes().length;
			final int[] splitIndices = IntStream.range(startInclusive, endExclusive).toArray();

			Plan child = isLeft ? leftChild : rightChild;
			final TupleType splitTupleType = TupleType.DefaultFactory
					.createFromTyped(child.getInputAttributes());

			this.tupleSplitter = tuple -> {
				Object[] values = new Object[splitIndices.length];
				for (int i = 0; i != splitIndices.length; i++)
					values[i] = tuple.getValues()[splitIndices[i]];
				return splitTupleType.createTuple(values);
			};
		}

		@Override
		public boolean hasNext() {
			return !this.queue.isEmpty() || this.inputTuples.hasNext();
		}

		@Override
		public Tuple next() {

			// Get the next tuple from the queue or, if that's empty, the inputTuples field.
			Tuple tuple;
			if (!this.queue.isEmpty())
				tuple = this.queue.remove();
			else
				tuple = this.inputTuples.next();

			// Add the tuple to the queue on the other child.
			this.otherQueue.add(tuple);

			// Apply the tuple-splitter function & return the result.
			return this.tupleSplitter.apply(tuple);
		}
	}
}
