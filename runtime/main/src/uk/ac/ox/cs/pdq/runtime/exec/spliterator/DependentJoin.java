package uk.ac.ox.cs.pdq.runtime.exec.spliterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import jersey.repackaged.com.google.common.base.Preconditions;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.Plan;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.runtime.exec.PlanDecorator;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;

public class DependentJoin extends BinaryExecutablePlan {

	// Fields to keep track of iteration over the left child.
	private Tuple leftTuple = null;
	private Tuple projectedLeftTuple = null;

	// Functions to project onto the left or right join attributes.
	private final Function<Tuple, Tuple> leftProjector;
	private final Function<Tuple, Tuple> rightProjector;

	// Fields to cache the results from the right child. 
	private List<Tuple> accessedRightTuplesCache = new ArrayList<Tuple>();
	private Map<Tuple, List<Tuple>> matchingRightTuplesCache = new HashMap<Tuple, List<Tuple>>();

	// Flags to indicate when to use the cached tuples from the right child.
	private boolean useAccessedRightTuplesCache = false;
	private boolean useMatchingRightTuplesCache = false;

	// Fields for batching inputs to the right child.
	private int batchSize = 100;

	private ArrayList<Tuple> leftTuplesBatch; // Concrete type used to ensure fail-fast iterators.
	private Iterator<Tuple> batchIterator;

	public boolean incompleteBatchDetected = false;

	// Fields to keep track of "external" dynamic input.
	private List<Tuple> inputTuplesList;
	private final List<Attribute> boundRightInputs;
	private final List<Attribute> unboundRightInputs;

	public DependentJoin(Plan plan, PlanDecorator decorator) throws Exception {
		super(plan,decorator);
		// Check compatibility with the given Plan instance.
		Preconditions.checkArgument(plan instanceof DependentJoinTerm);

		// Assign the function to project outputs from the left child onto the bound
		// input attributes of the right child (as a closure).
		this.leftProjector = ExecutablePlan.tupleProjector(this.leftChild.getOutputAttributes(), 
				((DependentJoinTerm) plan).boundAttributes());

		// Assign the function to project outputs from the right child onto its bound
		// input attributes (as a closure).
		this.rightProjector = ExecutablePlan.tupleProjector(this.rightChild.getOutputAttributes(), 
				((DependentJoinTerm) plan).boundAttributes());

		this.boundRightInputs = Arrays.asList(((DependentJoinTerm) this.getDecoratedPlan()).boundAttributes());
		this.unboundRightInputs = Arrays.stream(this.rightChild.getInputAttributes())
				.filter(attr -> !boundRightInputs.contains(attr)).collect(Collectors.toList());
	}

	@Override
	public Spliterator<Tuple> spliterator() {

		this.clearCache();
		Spliterator<Tuple> leftChildSpliterator = this.leftChild.spliterator();

		// TODO: reduce the number of passes over the left child tuples by assigning
		// the (projected, N-limited) left child spliterator directly to the dynamic 
		// input on the right child, with an action to populate the batch.

		// Initialise the batch of tuples from the left child. 
		this.replenishBatch(leftChildSpliterator);

		if (this.leftTuplesBatch.size() == 0)
			return Spliterators.emptySpliterator();
		
		// Assign the leftTuple and projectedLeftTuple fields. 
		this.assignLeftTuple();

		// Set the dynamic input on the right child (required before we call its spliterator method).
		this.assignRightChildInputs();

		return new DependentProductSpliterator(leftChildSpliterator, 
				this.rightChild.spliterator());
	}

	// Assigns the leftTuple and projectedLeftTuple fields. 
	private void assignLeftTuple() {
		Preconditions.checkState(this.batchIterator.hasNext());
		this.leftTuple = this.batchIterator.next(); 
		this.projectedLeftTuple = this.leftProjector.apply(this.leftTuple); 
	}

	// Replenishes the batch of tuples from the left child, resets the 
	// spliterator over that batch and returns false iff there are no 
	// more left tuples with which to replenish the batch.
	private boolean replenishBatch(Spliterator<Tuple> leftChildSpliterator) {

		this.clearBatch();
		this.leftTuplesBatch = StreamSupport.stream(leftChildSpliterator, false)
				.limit(this.batchSize)
				.collect(Collectors.toCollection(ArrayList::new));

		if (this.leftTuplesBatch.isEmpty()) 
			return false;

		// IMP TODO: INCLUDE THIS CHECK IFF IT IS VALID AND NECESSARY
		//			// There should be at most one incomplete batch.
		//			if (this.leftTuplesBatch.size() != this.batchSize) {
		//				Preconditions.checkState(!this.incompleteBatchDetected);
		//				this.incompleteBatchDetected = true;
		//			}

		this.batchIterator = this.leftTuplesBatch.iterator();
		return true;
	}

	private void clearBatch() {

		// Clear both the batch and the corresponding cache of accessed
		// tuples from the right child.
		this.leftTuplesBatch = null;
		this.accessedRightTuplesCache.clear();

		// Stop using the cache of accessed tuples from the right child.
		this.useAccessedRightTuplesCache = false;
	}

	private void assignRightChildInputs() {

		// If all of the inputs to the right child are bound from the left, assign the 
		// input tuples directly from the (projected) tuples in the leftTuplesBatch. 
		if (this.unboundRightInputs.size() == 0) {
			rightChild.setInputTuples(new ProjectedBatchIterator());
			return;
		}

		// If the right child takes dynamic input on attributes that are not bound from 
		// the left child, combine them with the projected tuples from the leftTuplesBatch.
		rightChild.setInputTuples(new CombinedInputsIterator());
	}

	// Updates the cache of matching tuples from the right child from the cache
	// of accessed tuples.
	private void updateMatchingRightTuplesCache() {

		for (Tuple tuple:this.accessedRightTuplesCache) {
			Tuple key = this.rightProjector.apply(tuple);

			// Only update the cache if the projected left tuple matches the key.
			if (!this.leftProjector.apply(this.leftTuple).equals(key)) {
				return;
			}

			if (this.matchingRightTuplesCache.containsKey(key))
				this.matchingRightTuplesCache.get(key).add(tuple);
			else {
				List<Tuple> value = new ArrayList<Tuple>();
				value.add(tuple);
				this.matchingRightTuplesCache.put(key, value);
			}
		}
	}

	// Override the setInputTuples method to hold a reference locally if necessary.
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
		while (inputTuples.hasNext())
			this.inputTuplesList.add(inputTuples.next());
		super.setInputTuples(this.inputTuplesList.iterator());
	}

	@Override
	public void close() {
		super.close();
		this.clearCache();
		this.leftTuple = null;
		this.projectedLeftTuple = null;
	}

	private void clearCache() {

		this.matchingRightTuplesCache.clear();
		this.accessedRightTuplesCache.clear();
		this.leftTuplesBatch = null;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		Preconditions.checkArgument(batchSize > 0);
		this.batchSize = batchSize;
	}

	private class DependentProductSpliterator extends BinaryPlanSpliterator {

		Tuple rightTuple;
		private final Consumer<Tuple> rightTupleAction = tuple -> this.rightTuple = tuple;

		public DependentProductSpliterator(Spliterator<Tuple> leftChildSpliterator, 
				Spliterator<Tuple> rightChildSpliterator) {
			super(leftChildSpliterator, rightChildSpliterator);
		}

		// Advances to the next left tuple and reset the spliterator over tuples
		// from the right child. Returns false iff there are no more left tuples.
		private boolean advanceLeftTuple() {

			// Try to advance over the leftTuplesBatch.
			if (batchIterator.hasNext()) {
				assignLeftTuple();
				this.resetRightChildSpliterator();
				return true;
			}

			// If the batch is exhausted, try to replenish it.
			if (!replenishBatch(this.leftChildSpliterator))
				return false;

			// If the batch was replenished, advance to the first tuple in the new batch.
			return this.advanceLeftTuple();
		}

		// Re-initialises the right child spliterator, either from:
		// - the matching tuples cache (if the current projectedLeftTuple is in that cache's key set),
		// - the accessed tuples cache (if it isn't, *and* the right child has already been traversed for this batch), or
		// - the right child plan (otherwise, in which case the current leftTuple is the first of a new batch).
		private void resetRightChildSpliterator() {

			useMatchingRightTuplesCache = matchingRightTuplesCache.containsKey(projectedLeftTuple);

			if (useMatchingRightTuplesCache) {
				this.rightChildSpliterator = matchingRightTuplesCache.get(projectedLeftTuple).spliterator();
				return;
			}

			if (useAccessedRightTuplesCache) {
				this.rightChildSpliterator = accessedRightTuplesCache.spliterator();
				return;
			}
			assignRightChildInputs();
			this.rightChildSpliterator = rightChild.spliterator();
		}

		@Override

			/* 
			 * Algorithm:
			 * - advance to the next right tuple; if the right child is exhausted:
			 * 		- update the cache of matching tuples from the right child
			 * 		- set the flag to use the cache of accessed right tuples
			 * 		- advance to the next left tuple and reset the right child spliterator
			 * - Add the right tuple to the cache of accessed right tuples (unless that cache is already in use) 
			 * - Prepend the current leftTuple to the current rightTuple
			 * - Test the join condition on the joined tuple
			 * 		- recursively call this method if the condition is not satisfied -- Gabor: I changed this to use a do-while loop instead of recursively calling itself, since there is no way of knowing how deep that recursion would go ( worst case scenario is that you call itself as many times as many tuples are created from a cartesian product, causing stack overflow )  
			 * - Pass the joined tuple to the given action & return true
			 */
		public boolean tryAdvance(Consumer<? super Tuple> action) {
			Tuple joinedTuple = null;
			boolean advance;
			do {
				advance = false;
				boolean rightIsExhausted = !rightChildSpliterator.tryAdvance(this.rightTupleAction);
	
				//			// TODO: RE-INCLUDE THIS (OR EXPLAIN WHY IT'S NOT A VALID CHECK): 
				//			// It should never be the case that right was exhausted and still is.
				//			Preconditions.checkState(!(rightWasExhausted && rightIsExhausted));
	
				if (rightIsExhausted) {
					if (!useAccessedRightTuplesCache && !useMatchingRightTuplesCache)
						updateMatchingRightTuplesCache();
					if (!useMatchingRightTuplesCache)
						useAccessedRightTuplesCache = true;
					if (!this.advanceLeftTuple()) // This also resets the right child spliterator.
						return false;
					advance = true;
				} else {
	
					Preconditions.checkState(this.rightTuple != null);
		
					if (!useAccessedRightTuplesCache)
						accessedRightTuplesCache.add(this.rightTuple);
		
					joinedTuple = leftTuple.appendTuple(this.rightTuple);
		
					if (!getJoinCondition().isSatisfied(joinedTuple)) {
						advance = true;
					}
				}
				//if we haven't found a matching tuple keep try to advance forward until we reach the end of both streams or a match found. 
			} while(advance);

			action.accept(joinedTuple);
			return true;
		}

		@Override
		public Spliterator<Tuple> trySplit() {
			// TODO: For parallelism benefit, implement this method by splitting the leftChildSpliterator.
			return null;
		}
	}

	// Iterates (and combines) over the cross product of the "external" dynamic 
	// input tuples and the projected tuples from the leftTuplesBatch.
	private class CombinedInputsIterator implements Iterator<Tuple> {

		private Iterator<Tuple> inputTuples;
		private ProjectedBatchIterator projectedBatchIterator;

		private Tuple currentInputTuple;

		TupleType tt = TupleType.createFromTyped(rightChild.getInputAttributes());

		CombinedInputsIterator() {

			Preconditions.checkState(inputTuplesList != null && inputTuplesList.size() != 0, 
					"Missing dynamic input");
			
			this.inputTuples = inputTuplesList.iterator();
			Preconditions.checkArgument(this.inputTuples.hasNext());
			this.currentInputTuple = this.inputTuples.next();

			this.refreshProjectedLeftTuples();
			Preconditions.checkArgument(this.projectedBatchIterator.hasNext());
		}

		private void refreshProjectedLeftTuples() {
			this.projectedBatchIterator = new ProjectedBatchIterator();
		}

		@Override
		public boolean hasNext() {
			return this.inputTuples.hasNext() || this.projectedBatchIterator.hasNext();
		}

		@Override
		public Tuple next() {
			// Get the current dynamic input tuple and projected left tuple.
			if (!this.projectedBatchIterator.hasNext()) {
				this.currentInputTuple = this.inputTuples.next();
				this.refreshProjectedLeftTuples();
			}
			Tuple currentProjectedLeftTuple = this.projectedBatchIterator.next();

			// Combine the inputTuples with the projected tuples from the leftTuplesBatch.
			Object[] values = new Object[tt.size()];
			int boundCount = 0;
			int dynamicCount = 0;
			for (int i = 0; i != rightChild.getInputAttributes().length; i++) {
				values[i] = boundRightInputs.contains(rightChild.getInputAttributes()[i]) ? 
						currentProjectedLeftTuple.getValue(boundCount++) : 
							this.currentInputTuple.getValue(dynamicCount++);
			}
			return tt.createTuple(values);
		}
	}

	// Iterates over the projected leftTuplesBatch. 
	private class ProjectedBatchIterator implements Iterator<Tuple> {

		private final Iterator<Tuple> batchIterator;

		public ProjectedBatchIterator() {
			this.batchIterator = leftTuplesBatch.iterator();
		}

		@Override
		public boolean hasNext() {
			return this.batchIterator.hasNext();
		}

		@Override
		public Tuple next() {
			return leftProjector.apply(this.batchIterator.next());
		}
	}

}
