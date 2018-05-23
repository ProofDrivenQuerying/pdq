package uk.ac.ox.cs.pdq.runtime.exec.spliterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import jersey.repackaged.com.google.common.base.Preconditions;
import uk.ac.ox.cs.pdq.algebra.CartesianProductTerm;
import uk.ac.ox.cs.pdq.algebra.Plan;
import uk.ac.ox.cs.pdq.runtime.exec.PlanDecorator;
import uk.ac.ox.cs.pdq.util.Tuple;

/**
 * An executable Cartesian product plan. 
 * 
 * @author Tim Hobson
 *
 */
public class CartesianProduct extends BinaryExecutablePlan {

	// Field to keep track of iteration over the left child.
	private Tuple leftTuple = null;

	// Consumer to assign tuples from the left child to the leftTuple field.
	private Consumer<? super Tuple> leftTupleConsumer = tuple -> this.leftTuple = tuple;
	
	// Fields to cache the results from the right child. 
	private List<Tuple> rightResultsCache = new ArrayList<Tuple>();
	private boolean useResultsCache = false;

	public CartesianProduct(Plan plan, PlanDecorator decorator) throws Exception {
		super(plan,decorator);
		// Check compatibility with the given Plan instance.
		Preconditions.checkArgument(plan instanceof CartesianProductTerm);
	}

	@Override
	public Spliterator<Tuple> spliterator() {
		
		this.clearCache();
		return new CartesianProductSpliterator(this.leftChild.spliterator(), 
				this.rightChild.spliterator());
	}
	
	@Override
	public void close() {
		super.close();
		this.clearCache();
		this.leftTuple = null;
	}

	private void clearCache() {
		this.rightResultsCache = new ArrayList<Tuple>();
		this.useResultsCache = false;
	}

	private class CartesianProductSpliterator extends BinaryPlanSpliterator {

		// Mapper to cache the results from the right child.
		private Function<Tuple, Tuple> cachingMapper = tuple -> {
			rightResultsCache.add(tuple);
			return tuple;
		};

		public CartesianProductSpliterator(Spliterator<Tuple> leftChildSpliterator, 
				Spliterator<Tuple> rightChildSpliterator) {
			super(leftChildSpliterator, rightChildSpliterator);
			
			// Initialise the leftTuple field. 
			this.leftChildSpliterator.tryAdvance(leftTupleConsumer);
		}

		private boolean advanceLeftTuple() {
			
			Tuple lastLeftTuple = leftTuple;
			
			// If both children are exhausted, terminate.
			if (!this.leftChildSpliterator.tryAdvance(leftTupleConsumer))
				return false;

			// If the leftTuple did not change, recurse.
			if (leftTuple.equals(lastLeftTuple))
				return this.advanceLeftTuple();
			
			return true;
		}
		
		@Override
		public boolean tryAdvance(Consumer<? super Tuple> action) {

			/* 
			 * Algorithm:
			 * - Stream over the right child plan or its cache if available (in the former case, cache the results):
			 * 		- prepend the current leftTuple, and 
			 * 		- delegate the given action to the stream.
			 * - When the stream is exhausted: 
			 * 		- advance to the next left tuple, and 
			 * 		- re-initialise the right child from the cache.
			 * - Continue until the left child is exhausted.
			 */

			// Stream over tuples from the right child.
			Stream<Tuple> stream = StreamSupport.stream(this.rightChildSpliterator, false);

			// If we are not already using the right child results cache, add an 
			// action to update the cache by adding each streamed tuple.
			if (!useResultsCache)
				stream = stream.map(this.cachingMapper);

			// Append to the current left tuple & delegate the given action to the stream.
			if (stream.map(tuple -> leftTuple.appendTuple(tuple))
					.spliterator().tryAdvance(action))
				return true;

			// Advance to the next left tuple. Terminate if both children are exhausted.
			if (!this.advanceLeftTuple())
				return false;

			// If only the right child is exhausted, advance to the next left tuple 
			// & re-initialise the right child spliterator from the results cache.
			this.rightChildSpliterator = rightResultsCache.spliterator();
			useResultsCache = true;
			
			return true;
		}

		@Override
		public Spliterator<Tuple> trySplit() {
			// TODO: For parallelism benefit, implement this method by splitting the leftChildSpliterator.
			return null;
		}
	}
}
