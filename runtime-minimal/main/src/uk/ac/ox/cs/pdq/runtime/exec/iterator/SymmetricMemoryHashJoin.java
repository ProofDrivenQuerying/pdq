package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.SimpleCondition;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.util.Tuple;


/**
 * SymmetricMemoryHashJoin inspired from the ViP2P implementation. The buckets are build on
 * the left operator, and the iteration run over the right one.
 * 
 * @author Julien Leblay
 */
public class SymmetricMemoryHashJoin extends Join {

	/** The left hand hash table. */
	protected Multimap<JoinKey, Tuple> leftHashTable = LinkedListMultimap.create();

	/** The right hand hash table. */
	protected Multimap<JoinKey, Tuple> rightHashTable = LinkedListMultimap.create();

	/** Tuple iterator on the current bucket. */
	protected Iterator<Tuple> bucketIterator = null;

	/**  The partial next tuple to return. */
	protected Tuple partialTuple = null;

	/** The join keys for the left child. */
	protected Integer[] joinKeysForLeftChild;

	/** The join keys for the right child. */
	protected Integer[] joinKeysForRightChild;

	/**  Index of the last child. */
	protected int lastChild = 1;

	protected boolean side = true;
	
	public SymmetricMemoryHashJoin(TupleIterator child1, TupleIterator child2) {
		super(child1, child2);
		this.joinKeysForLeftChild = this.makeLeftKey();
		this.joinKeysForRightChild = this.makeRightKey(child1.getNumberOfOutputAttributes());
	}

	@Override
	public void reset() {
		super.reset();
		this.leftHashTable.clear();
		this.rightHashTable.clear();
	}

	@Override
	protected void nextTuple() {
		while (this.bucketIterator == null || !this.bucketIterator.hasNext()) {
			TupleIterator child = this.children[0];
			Integer[] keys = this.joinKeysForLeftChild;
			Multimap<JoinKey, Tuple> table = this.leftHashTable;
			Multimap<JoinKey, Tuple> table2 = this.rightHashTable;
			if (!this.side) {
				child = this.children[1];
				keys  = this.joinKeysForRightChild;
				table = this.rightHashTable;
				table2 = this.leftHashTable;
			}
			if (child.hasNext()) {
				Tuple t = child.next();
				JoinKey joinKey = this.joinKeys(keys, t);
				if (joinKey != null) {
					table.put(joinKey, t);
					Collection<Tuple> tuples = table2.get(joinKey);
					if (!tuples.isEmpty()) {
						this.partialTuple = t;
						this.bucketIterator = tuples.iterator();
					}
				}
			}
			this.side = !this.side;
			if (!this.children[0].hasNext() && !this.children[1].hasNext()) {
				break;
			}
		}
		if (this.bucketIterator != null) {
			if (!this.bucketIterator.hasNext()) {
				this.bucketIterator = null;
				this.nextTuple = null;
				return;
			}
			if (this.side) 
				this.nextTuple = this.outputTupleType.appendTuples(this.bucketIterator.next(), this.partialTuple);
			else 
				this.nextTuple = this.outputTupleType.appendTuples(this.partialTuple, this.bucketIterator.next()); 
			
		}
	}

	/**
	 * Build a variableName-to-value binding from the given join key and tuple.
	 *
	 * @param keys the keys
	 * @param tuple the tuple
	 * @return JoinKey
	 */
	protected JoinKey joinKeys(Integer[] keys, Tuple tuple) {
		List<Object> result = new ArrayList<>(keys.length);
		for (Integer i: keys) {
			result.add(tuple.getValue(i));
		}
		return new JoinKey(result);
	}

	/**
	 * Returns an integer to Variable maps representing the position in arg1
	 * of each variable appearing in both arg1 and arg2.
	 * 
	 * @param left List<? extends Term>
	 * @param right Collection<? extends Term>
	 * @return the position in arg1 of variable appearing in both arg1 and arg2
	 */
	protected Map<Integer, Term> joinColumns(List<? extends Term> left, Collection<? extends Term> right) {
		Map<Integer, Term> result = new LinkedHashMap<>();

		List<Term> inter = new ArrayList<>(right);
		inter.retainAll(left);
		for (Term t : inter) {
			for (int i = 0, l = left.size(); i < l; i++) {
				if (left.get(i).equals(t)) {
					result.put(i, t);
				}
			}
		}
		return result;
	}
	
	private Iterable<AttributeEqualityCondition> listAttributeEqualityPredicates(Condition predicate) {
		Set<AttributeEqualityCondition> result = new LinkedHashSet<>();
		if (predicate instanceof ConjunctiveCondition) {
			ConjunctiveCondition conjunction = (ConjunctiveCondition) predicate;
			for (SimpleCondition subPred : conjunction.getSimpleConditions()) {
				for (AttributeEqualityCondition p: listAttributeEqualityPredicates(subPred)) {
					result.add(p);
				}
			}
		} else  if (predicate instanceof AttributeEqualityCondition) {
			result.add((AttributeEqualityCondition) predicate);
		} else if (!(predicate instanceof ConstantEqualityCondition)) {
			throw new UnsupportedOperationException("Unsupported predicate type " + predicate);
		}
		return result;
	}

	protected Integer[] makeLeftKey() {
		List<Integer> result = new ArrayList<>();
		for (AttributeEqualityCondition p: listAttributeEqualityPredicates(this.joinConditions)) {
			result.add(p.getPosition());
		}
		return result.toArray(new Integer[result.size()]);
	}

	protected Integer[] makeRightKey(int offset) {
		List<Integer> result = new ArrayList<>();
		for (AttributeEqualityCondition p: listAttributeEqualityPredicates(this.joinConditions)) {
			result.add(p.getOther() - offset);
		}
		return result.toArray(new Integer[result.size()]);
	}

	/**
	 * JoinKey is the representation of a key during the execution of a hash
	 * join. A key is typically a mapping from column names to values.
	 * @author Julien LEBLAY
	 */
	protected class JoinKey {

		/**  Mapping between columns names and values. */
		private final List<Object> joinValues;
		
		/** The hash code. */
		private final int hashCode;

		/**
		 * Instantiates a JoinKey form a tuple of bindings.
		 *
		 * @param joinValues the join values
		 */
		JoinKey(List<Object> joinValues) {
			this.joinValues = joinValues;
			this.hashCode = Objects.hash(joinValues);
		}

		@Override
		public int hashCode() {
			return this.hashCode;
		}

		@Override
		public boolean equals(Object o ) {
			if (this == o) {
				return true;
			}
			return this.getClass().isInstance(o)
					&& ((JoinKey) o).joinValues.equals(this.joinValues);
		}

		@Override
		public String toString() {
			return this.joinValues.toString();
		}
	}
}
