// DEPRECATED

package uk.ac.ox.cs.pdq.runtime.exec.spliterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import jersey.repackaged.com.google.common.base.Preconditions;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.Plan;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;

/**
 * An executable dependent product plan.
 * 
 * A dependent product is similar to a Cartesian product, except that the right 
 * child must contain input attributes whose values are supplied dynamically
 * from the results of executing the left child plan.
 * 
 * @author Tim Hobson
 */
public class DependentJoinUnbatched extends BinaryExecutablePlan {

	// Fields to keep track of iteration over the left child.
	private Tuple leftTuple = null;
	private Tuple projectedLeftTuple = null;
	private final Function<Tuple, Tuple> projector;

	// Consumer to assign tuples to the leftTuple & projectedLeftTuple fields.
	private final Consumer<? super Tuple> leftTupleConsumer;

	// Fields to cache the results from the right child. 
	private Map<Tuple, List<Tuple>> rightResultsCache = new HashMap<Tuple, List<Tuple>>();
	private List<Tuple> intermediateCache = new ArrayList<Tuple>();
	private boolean useResultsCache = false;

	// Mapper to cache the results from the right child.
	private final Function<Tuple, Tuple> cachingMapper = tuple -> {
		this.intermediateCache.add(tuple);
		return tuple;
	};

	// Fields to keep track of dynamic input.
	private List<Tuple> inputTuplesList;
	private final List<Attribute> boundRightInputs;
	private final List<Attribute> unboundRightInputs;

	public DependentJoinUnbatched(Plan plan) {
		super(plan);
		// Check compatibility with the given Plan instance.
		Preconditions.checkArgument(plan instanceof DependentJoinTerm);

		// Assign the function to project outputs from the left child onto the bound
		// input attributes of the right child (as a closure).
		this.projector = ExecutablePlan.tupleProjector(this.leftChild.getOutputAttributes(), 
				((DependentJoinTerm) plan).boundAttributes());

		this.leftTupleConsumer = tuple -> { 
			this.leftTuple = tuple; 
			this.projectedLeftTuple = this.projector.apply(tuple); 
		};

		this.boundRightInputs = Arrays.asList(((DependentJoinTerm) this.getDecoratedPlan()).boundAttributes());
		this.unboundRightInputs = Arrays.stream(this.rightChild.getInputAttributes())
				.filter(attr -> !boundRightInputs.contains(attr)).collect(Collectors.toList());
		
		// The inputTuplesList field is used only if the right child has input 
		// attributes which are not bound from the left child. 
		if (this.unboundRightInputs.size() != 0)
			inputTuplesList = new ArrayList<Tuple>();
	}

	@Override
	public Spliterator<Tuple> spliterator() {

		this.clearCache();
		Spliterator<Tuple> leftChildSpliterator = this.leftChild.spliterator();

		// Initialise the leftTuple and projectedLeftTuple fields. 
		Preconditions.checkState(leftChildSpliterator.tryAdvance(leftTupleConsumer));
		// Set the dynamic input on the right child (required before we call its spliterator method).
		this.assignRightChildInputs();

		return new DependentProductSpliterator(leftChildSpliterator, 
				this.rightChild.spliterator());
	}

	private void assignRightChildInputs() {

		// If all of the inputs to the right child are bound from the left, assign the 
		// input tuples directly from the projectedLeftTuple. 
		if (this.unboundRightInputs.size() == 0) {
			rightChild.setInputTuples(Arrays.asList(this.projectedLeftTuple).iterator());
			return;
		}
		
		// If the right child takes dynamic input on attributes that are not bound from 
		// the left child, combine them with the projected left tuple.
		rightChild.setInputTuples(new CombinedInputsIterator());
	}

	// Override the setInputTuples method to hold a reference locally.
	@Override
	public void setInputTuples(Iterator<Tuple> inputTuples) {

		// Unless there are unbound inputs to the right child, just delegate to the superclass.
		if (this.unboundRightInputs.size() == 0) {
			super.setInputTuples(inputTuples);
			return;
		}
		
		// If the right child has unbound inputs, maintain a list of all input 
		// tuples to this plan so that these can be "refreshed" every time the
		// right child spliterator is re-initialised.
		this.inputTuplesList = new ArrayList<Tuple>();
		while(inputTuples.hasNext())
			this.inputTuplesList.add(inputTuples.next());
		super.setInputTuples(this.inputTuplesList.iterator());
	}

	@Override
	public void close() {
		super.close();
		this.clearCache();
		this.leftTuple = null;
	}
	
	private void clearCache() {
		this.rightResultsCache.clear();
		this.intermediateCache.clear();
	}

	private class DependentProductSpliterator extends BinaryPlanSpliterator {

		public DependentProductSpliterator(Spliterator<Tuple> leftChildSpliterator, 
				Spliterator<Tuple> rightChildSpliterator) {
			super(leftChildSpliterator, rightChildSpliterator);
		}

		private boolean advanceLeftTuple() {
			
			Tuple lastLeftTuple = leftTuple;
			
			// Try to advance the left child spliterator. If exhausted, return false.
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
			 * - Stream over the right child plan or its cache for the current projected tuple if available 
			 *   (in the former case, build an intermediate cache of the results)
			 * 		- prepend the current leftTuple, and
			 * 		- delegate the given action to the stream.
			 * - When the stream is exhausted: 
			 * 		- if the cache is not currently being used:
			 * 			- move the intermediate cache to the rightResultsCache, using the projectedLeftTuple as the key, and
			 * 			- clear the intermediate cache;
			 * 		- advance to the next left tuple,
			 * 		- re-set the dynamic input on the right child (using the new projected left tuple), and
			 * 		- re-initialise the right child spliterator.
			 * - Continue until the left child is exhausted.
			 * 
			 * Note: an alternative algorithm would be to first get all input tuples to the right 
			 * child by first executing the entire left child plan. This would avoid the need for 
			 * the rightResultsCache Map, but would not be pipelined (which is a requirement).  
			 */

			// Stream over tuples from the right child.
			Stream<Tuple> stream = StreamSupport.stream(rightChildSpliterator, false);

			// If we are not already using cached results, add an action to update the cache by adding each streamed tuple.
			if (!useResultsCache)
				stream = stream.map(cachingMapper);

			// Append to the current left tuple, filter on the join condition 
			// and delegate the given action to the stream. Note that the filter
			// is necessary even though the join condition is known to be satisfied
			// on the bound attributes, since the condition may also include other
			// attributes which are unbound (i.e. are not inputs to the right child). 
			if (stream.map(tuple -> leftTuple.appendTuple(tuple))
					.filter(tuple -> getJoinCondition(tuple).isSatisfied(tuple))
					.spliterator().tryAdvance(action))
				return true;

			// If only the right child is exhausted and we are not already using the cache, 
			// update the rightResultsCache and refresh the intermediate cache.
			if (!useResultsCache) {
				rightResultsCache.put(projectedLeftTuple, intermediateCache);
				intermediateCache = new ArrayList<Tuple>();
			}

			// Advance to the next left tuple. Terminate if both children are exhausted.
			if (!this.advanceLeftTuple())
				return false;
			
			// If only the right child is exhausted, advance to the next left 
			// tuple and re-initialise the right child spliterator, either from 
			// the cache or from the right child plan.
			useResultsCache = rightResultsCache.containsKey(projectedLeftTuple);
			if (useResultsCache)
				this.rightChildSpliterator = rightResultsCache.get(projectedLeftTuple).spliterator();
			else {
				assignRightChildInputs();
				this.rightChildSpliterator = rightChild.spliterator();
			}
			return true;
		}

		@Override
		public Spliterator<Tuple> trySplit() {
			// TODO: For parallelism benefit, implement this method by splitting the leftChildSpliterator.
			return null;
		}
	}
	
	private class CombinedInputsIterator implements Iterator<Tuple> {

		private Iterator<Tuple> inputTuples;

		TupleType tt = TupleType.createFromTyped(rightChild.getInputAttributes());

		CombinedInputsIterator() {
			this.inputTuples = inputTuplesList.iterator();
		}
		
		@Override
		public boolean hasNext() {
			return this.inputTuples.hasNext();
		}

		@Override
		public Tuple next() {
			// Combine the inputTuples with the projectedLeftTuple.
			Tuple dynamicInput = this.inputTuples.next();
			Object[] values = new Object[tt.size()];
			int boundCount = 0;
			int dynamicCount = 0;
			for (int i = 0; i != rightChild.getInputAttributes().length; i++) {
				values[i] = boundRightInputs.contains(rightChild.getInputAttributes()[i]) ? 
						projectedLeftTuple.getValue(boundCount++) : 
							dynamicInput.getValue(dynamicCount++);
			}
			return tt.createTuple(values);
		}
	}
}
