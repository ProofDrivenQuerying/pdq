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

import uk.ac.ox.cs.pdq.algebra.AttributeEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.SimpleCondition;
import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;


// TODO: Auto-generated Javadoc
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
	protected Integer[] leftKeys;

	/** The join keys for the right child. */
	protected Integer[] rightKeys;

	/**  Index of the last child. */
	protected int lastChild = 1;

	/** The side. */
	protected boolean side = true;

	/** The left. */
	protected final TupleIterator left;

	/** The right. */
	protected final TupleIterator right;

	/**
	 * Constructor an unbound array of children.
	 *
	 * @param left TupleIterator
	 * @param right TupleIterator
	 */
	public SymmetricMemoryHashJoin(TupleIterator left, TupleIterator right) {
		this(computeNaturalJoinConditions(toList(left, right)), 
			inferInputColumns(toList(left, right)), left, right);
	}

	/**
	 * Constructor an unbound array of children.
	 *
	 * @param predicate ConjunctivePredicate<AttributeEqualityPredicate>
	 * @param left TupleIterator
	 * @param right TupleIterator
	 */
	public SymmetricMemoryHashJoin(
			Condition predicate, TupleIterator left, TupleIterator right) {
		this(predicate, inferInputColumns(toList(left, right)), left, right);
	}

	/**
	 * Constructor an unbound array of children.
	 *
	 * @param inputs List<Typed>
	 * @param left TupleIterator
	 * @param right TupleIterator
	 */
	public SymmetricMemoryHashJoin(
			List<Typed> inputs, TupleIterator left, TupleIterator right) {
		this(computeNaturalJoinConditions(toList(left, right)), inputs, left, right);
	}

	/**
	 * Constructor an unbound array of children.
	 *
	 * @param predicate ConjunctivePredicate<AttributeEqualityPredicate>
	 * @param inputs List<Typed>
	 * @param left TupleIterator
	 * @param right TupleIterator
	 */
	public SymmetricMemoryHashJoin(
			Condition predicate,
			List<Typed> inputs,
			TupleIterator left, TupleIterator right) {
		super(predicate, inputs, toList(left, right));
		this.left = left;
		this.right = right;
		this.leftKeys = this.makeLeftKey();
		this.rightKeys = this.makeRightKey(left.getType().size());
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.Join#nextTuple()
	 */
	@Override
	protected void nextTuple() {
		while (this.bucketIterator == null || !this.bucketIterator.hasNext()) {
			TupleIterator child = this.left;
			Integer[] keys = this.leftKeys;
			Multimap<JoinKey, Tuple> table = this.leftHashTable;
			Multimap<JoinKey, Tuple> table2 = this.rightHashTable;
			if (!this.side) {
				child = this.right;
				keys  = this.rightKeys;
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
			if (!this.left.hasNext() && !this.right.hasNext()) {
				break;
			}
		}
		if (this.bucketIterator != null) {
			if (!this.bucketIterator.hasNext()) {
				this.bucketIterator = null;
				this.nextTuple = null;
				return;
			}
			if (this.side) {
				this.nextTuple = this.outputType.appendTuples(this.bucketIterator.next(), this.partialTuple);
			} else {
				this.nextTuple = this.outputType.appendTuples(this.partialTuple, this.bucketIterator.next());
			}
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
	protected Map<Integer, Term> joinColumns(
			List<? extends Term> left, Collection<? extends Term> right) {
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
	
	/**
	 * List attribute equality predicates.
	 *
	 * @param predicate the predicate
	 * @return the iterable
	 */
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

	/**
	 * Make left key.
	 *
	 * @return Integer[]
	 */
	protected Integer[] makeLeftKey() {
		List<Integer> result = new ArrayList<>();
		for (AttributeEqualityCondition p: listAttributeEqualityPredicates(this.predicate)) {
			result.add(p.getPosition());
		}
		return result.toArray(new Integer[result.size()]);
	}

	/**
	 * Make right key.
	 *
	 * @param offset int
	 * @return Integer[]
	 */
	protected Integer[] makeRightKey(int offset) {
		List<Integer> result = new ArrayList<>();
		for (AttributeEqualityCondition p: listAttributeEqualityPredicates(this.predicate)) {
			result.add(p.getOther() - offset);
		}
		return result.toArray(new Integer[result.size()]);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.NaryIterator#reset()
	 */
	@Override
	public void reset() {
		super.reset();
		this.leftHashTable.clear();
		this.rightHashTable.clear();
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

		/**
		 * Hash code.
		 *
		 * @return int
		 */
		@Override
		public int hashCode() {
			return this.hashCode;
		}

		/**
		 * Equals.
		 *
		 * @param o Object
		 * @return boolean
		 */
		@Override
		public boolean equals(Object o ) {
			if (this == o) {
				return true;
			}
			return this.getClass().isInstance(o)
					&& ((JoinKey) o).joinValues.equals(this.joinValues);
		}

		/**
		 * To string.
		 *
		 * @return String
		 */
		@Override
		public String toString() {
			return this.joinValues.toString();
		}
	}
}
