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
import uk.ac.ox.cs.pdq.runtime.exec.PlanDecorator;
import uk.ac.ox.cs.pdq.util.Tuple;

/**
 * Very similar to the SymmetricMemoryHashJoin but with a more readable implementation. 
 * Currently this version is not in use, but can be used for debugging. 
 * 
 * An executable plan implementing the symmetric memory hash join algorithm.
 * 
 * Symmetric Hash Join The traditional hash join algorithm is implemented in two
 * phases. During the first phase, all tuples are read from one of the two
 * inputs into an in-memory hash table. During the second phase, each tuple t
 * from the second input is hashed, and compared to tuples that hash to the same
 * value from the first input. Each tuple that matches t on the join attribute
 * is combined with t and output. As we have discussed, blocking on one input
 * until it has been read entirely, as hash join does on its first input, is
 * inappropriate for processing unbounded data streams. Symmetric hash join
 * is an enhancement to the traditional hash join that outputs
 * results as it reads from both inputs. It maintains a hash table for each
 * input. Tuples from each input are cached in the corresponding hash table, and
 * then matched with tuples in the other hash table. For each match, the joined
 * tuple is passed on. This algorithm has the desirable property that it returns
 * results while it is reading data. However, it accumulates a lot of state in
 * its hash tables.
 * 
 * @author gabor
 */
public class SymmetricMemoryHashJoinReadable extends BinaryExecutablePlan {

	// Maintain maps for each child with
	// - key: each unique projected sub-tuple (corresponding to the join attributes)
	// found in the child;
	// - value: the set of unique tuples corresponding to the key found in the
	// child.
	private Map<Tuple, Set<Tuple>> leftMap = new HashMap<Tuple, Set<Tuple>>();
	private Map<Tuple, Set<Tuple>> rightMap = new HashMap<Tuple, Set<Tuple>>();
	private boolean readingLeft=true;

	private Tuple newTuple;

	// Consumer to assign tuples from the active child to the activeTuple field.
	private Consumer<? super Tuple> activeTupleConsumer = tuple -> {
		this.newTuple = tuple;
	};

	// Functions to project onto the left or right join attributes.
	private final Function<Tuple, Tuple> leftProjector;
	private final Function<Tuple, Tuple> rightProjector;

	public SymmetricMemoryHashJoinReadable(Plan plan, PlanDecorator decorator) throws Exception {
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
		return new SymmetricMemoryHashJoinSpliterator(this.leftChild.spliterator(), this.rightChild.spliterator());
	}

	@Override
	public void close() {
		super.close();
		this.leftMap = new HashMap<Tuple, Set<Tuple>>();
		this.rightMap = new HashMap<Tuple, Set<Tuple>>();
	}

	private class SymmetricMemoryHashJoinSpliterator extends BinaryPlanSpliterator {
		public SymmetricMemoryHashJoinSpliterator(Spliterator<Tuple> leftChildSpliterator,
				Spliterator<Tuple> rightChildSpliterator) {
			super(leftChildSpliterator, rightChildSpliterator);
		}

		@Override
		public boolean tryAdvance(Consumer<? super Tuple> action) {
			Preconditions.checkNotNull(action);
			boolean leftIsExhausted = false;
			boolean rightIsExhausted = false;
			while (!leftIsExhausted || !rightIsExhausted) {
				if (readingLeft && !leftIsExhausted) {
					leftIsExhausted = !leftChildSpliterator.tryAdvance(activeTupleConsumer);
					System.out.println("Received left: " + newTuple);
					if (newTuple==null && leftIsExhausted) {
						newTuple = null;
						readingLeft = !readingLeft;
						continue;
					}
					Tuple projectedTuple = leftProjector.apply(newTuple);
					// Add the active tuple to the activeMap, using the projected tuple as the key.
					if (leftMap.containsKey(projectedTuple))
						leftMap.get(projectedTuple).add(newTuple);
					else
						leftMap.put(projectedTuple, Sets.newHashSet(newTuple));
					if (rightMap.containsKey(projectedTuple)) {
						// we have something to join
						Stream<Tuple> streamOfMatches = StreamSupport.stream(rightMap.get(projectedTuple).spliterator(), false);
						streamOfMatches = streamOfMatches.map(tuple -> newTuple.appendTuple(tuple));
						Spliterator<Tuple> spliterator = streamOfMatches.spliterator();
						while (spliterator.tryAdvance(action)) {
							System.out.println("advencing output");
						};
					} else {
						System.out.println("    filtering out: " +projectedTuple + " from " + newTuple);
					}
				} else if (!readingLeft && !rightIsExhausted) {
					rightIsExhausted = !rightChildSpliterator.tryAdvance(activeTupleConsumer);
					System.out.println("Received right: " + newTuple);
					if (newTuple==null && rightIsExhausted) {
						newTuple = null;
						readingLeft = !readingLeft;
						continue;
					}
					Tuple projectedTuple = rightProjector.apply(newTuple);
					// Add the active tuple to the activeMap, using the projected tuple as the key.
					if (rightMap.containsKey(projectedTuple))
						rightMap.get(projectedTuple).add(newTuple);
					else
						rightMap.put(projectedTuple, Sets.newHashSet(newTuple));
					if (leftMap.containsKey(projectedTuple)) {
						// we have something to join
						Stream<Tuple> streamOfMatches = StreamSupport.stream(leftMap.get(projectedTuple).spliterator(), false);
						streamOfMatches = streamOfMatches.map(tuple -> tuple.appendTuple(newTuple));
						Spliterator<Tuple> spliterator = streamOfMatches.spliterator();
						while (spliterator.tryAdvance(action)) {
							System.out.println("advencing output");
						};
					} else {
						System.out.println("    filtering out: " +projectedTuple + " from " + newTuple);
					}
				}
				newTuple = null;
				readingLeft = !readingLeft;
			}
			return false;
		}

		@Override
		public Spliterator<Tuple> trySplit() {
			// TODO: Implement this method for parallelism benefit.
			return null;
		}
	}
}
