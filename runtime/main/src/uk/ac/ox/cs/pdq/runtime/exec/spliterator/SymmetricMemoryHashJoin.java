// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.runtime.exec.spliterator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.collect.Sets;

import jersey.repackaged.com.google.common.base.Preconditions;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.Plan;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import uk.ac.ox.cs.pdq.runtime.exec.PlanDecorator;

/**
 * An executable plan implementing the symmetric memory hash join algorithm. 
 * 
 * @author Tim Hobson
 */
public class SymmetricMemoryHashJoin extends BinaryExecutablePlan {

	private boolean leftChildIsActive = true;

	// Maintain maps for each child with
	//   - key: each unique projected sub-tuple (corresponding to the join attributes) found in the child;
	//   - value: the set of unique tuples corresponding to the key found in the child.
	private Map<Tuple, Set<Tuple>> leftMap = new HashMap<Tuple, Set<Tuple>>();
	private Map<Tuple, Set<Tuple>> rightMap = new HashMap<Tuple, Set<Tuple>>();

	private Tuple activeTuple;
	private boolean activeTupleFlag;
	private Spliterator<Tuple> probedTuples = null;  

	// Consumer to assign tuples from the active child to the activeTuple field.
	private Consumer<? super Tuple> activeTupleConsumer = tuple -> {
		this.activeTupleFlag = !this.activeTupleFlag;
		this.activeTuple = tuple;
	};

	// Functions to project onto the left or right join attributes.
	private final Function<Tuple, Tuple> leftProjector;
	private final Function<Tuple, Tuple> rightProjector;

	public SymmetricMemoryHashJoin(Plan plan, PlanDecorator decorator) throws Exception {
		super(plan, decorator);
		// Check compatibility with the given Plan instance.
		Preconditions.checkArgument(plan instanceof JoinTerm);

		// Assign the projection functions (as closures).
		final Map<Attribute, Attribute> joinMap = ((JoinTerm) plan).joinMap();
		this.leftProjector = ExecutablePlan.tupleProjector(plan.getChildren()[0].getOutputAttributes(), 
				joinMap.keySet().stream().toArray(Attribute[]::new));
		this.rightProjector = ExecutablePlan.tupleProjector(plan.getChildren()[1].getOutputAttributes(), 
				joinMap.values().stream().toArray(Attribute[]::new));
	}

	@Override
	public Spliterator<Tuple> spliterator() {
		return new SymmetricMemoryHashJoinSpliterator(this.leftChild.spliterator(), 
				this.rightChild.spliterator());
	}

	@Override
	public void close() {
		super.close();
		this.leftMap = new HashMap<Tuple, Set<Tuple>>();
		this.rightMap = new HashMap<Tuple, Set<Tuple>>();
		this.probedTuples = null;
	}

	private class SymmetricMemoryHashJoinSpliterator extends BinaryPlanSpliterator {

		public SymmetricMemoryHashJoinSpliterator(Spliterator<Tuple> leftChildSpliterator, 
				Spliterator<Tuple> rightChildSpliterator) {
			super(leftChildSpliterator, rightChildSpliterator);
		}

		@Override
		public boolean tryAdvance(Consumer<? super Tuple> action) {
			Preconditions.checkNotNull(action);

			/* 
			 * Algorithm:
			 * - While the probedTuples field is null:
			 * 		- toggle the active child (and related fields: activeSpliterator, activeMap, probeMap, projector),
			 * 		- advance to the next active tuple:
			 * 			- terminate if both child spliterators are exhausted.
			 * 			- project the activeTuple onto the join attributes,
			 * 			- add the activeTuple to the activeMap, using the projected tuple as the key,
			 * 			- if the projected tuple is already in the key-set of the probeMap:
			 * 				- assign probedTuples ready for streaming over the corresponding set of cached tuples,  
			 * - Stream over the probedTuples
			 * 		- prepend the activeTuple if the left child is active, otherwise append it, and
			 * 		- delegate the given action to the stream.
			 * - When the stream is exhausted, set probedTuples to null and continue.
			 */

			boolean lastIsExhausted = false;
			boolean currentIsExhausted = false;
			while (probedTuples == null) {

				leftChildIsActive = !leftChildIsActive;

				Spliterator<Tuple> activeSpliterator = leftChildIsActive ? leftChildSpliterator : rightChildSpliterator;
				Map<Tuple, Set<Tuple>> activeMap = leftChildIsActive ? leftMap : rightMap;
				Map<Tuple, Set<Tuple>> probeMap = leftChildIsActive ? rightMap : leftMap;
				Function<Tuple, Tuple> projector = leftChildIsActive ? leftProjector : rightProjector;

				boolean lastActiveTupleFlag = activeTupleFlag;
				
				currentIsExhausted = !activeSpliterator.tryAdvance(activeTupleConsumer);

				if (lastIsExhausted && currentIsExhausted)
					return false;

				// If the activeTuple did not change, return true to continue.
				if (!currentIsExhausted && activeTupleFlag == lastActiveTupleFlag)
					return true;

				if (!currentIsExhausted) {
					// Project the activeTuple onto the join attributes.
					Tuple projectedTuple = projector.apply(activeTuple);

					// Add the active tuple to the activeMap, using the projected tuple as the key.
					if (activeMap.containsKey(projectedTuple))
						activeMap.get(projectedTuple).add(activeTuple);
					else
						activeMap.put(projectedTuple, Sets.newHashSet(activeTuple));

					// If the projected tuple is found in the probeMap, set probedTuples in 
					// preparation for streaming over the corresponding value.
					if (probeMap.containsKey(projectedTuple))
						probedTuples = probeMap.get(projectedTuple).spliterator();
				}

				lastIsExhausted = currentIsExhausted;
			}

			// Stream over the probedTuples (which is guaranteed not null by now).
			Stream<Tuple> stream = StreamSupport.stream(probedTuples, false);

			// Prepend or append the activeTuple
			if (leftChildIsActive)
				stream = stream.map(tuple -> activeTuple.appendTuple(tuple));
			else
				stream = stream.map(tuple -> tuple.appendTuple(activeTuple));

			// If the probedTuples stream is exhausted, set probedTuples to null.
			if (!stream.spliterator().tryAdvance(action))
				probedTuples = null;

			return true;
		}

		@Override
		public Spliterator<Tuple> trySplit() {
			// TODO: Implement this method for parallelism benefit.
			return null;
		}
	}
}
