package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import uk.ac.ox.cs.pdq.algebra.AttributeEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.SimpleCondition;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;


// TODO: Auto-generated Javadoc
/**
 * Join is a top-level class for all join implementations.
 * 
 * @author Julien Leblay
 */
public abstract class Join extends NaryIterator {

	/** The predicate. */
	protected Condition predicate;
	
	/** Determines whether the operator is known to have an empty result. */
	protected boolean isEmpty = false;
	
	/** The next tuple to return. */
	protected Tuple nextTuple = null;

	/**
	 * Instantiates a new join.
	 *
	 * @param inputs List<Typed>
	 * @param children            the children
	 */
	protected Join(List<Typed> inputs, TupleIterator... children) {
		this(inferNaturalJoin(Lists.newArrayList(children)), inputs, Lists.newArrayList(children));
	}
	
	/**
	 * Instantiates a new join.
	 *
	 * @param inputs List<Typed>
	 * @param children            the children
	 */
	protected Join(List<Typed> inputs, List<TupleIterator> children) {
		this(inferNaturalJoin(children), inputs, children);
	}
	
	/**
	 * Instantiates a new join.
	 * @param predicate Atom
	 * @param inputs List<Typed>
	 * @param children
	 *            the children
	 */
	protected Join(Condition predicate, List<Typed> inputs, Collection<TupleIterator> children) {
		super(TupleType.DefaultFactory.createFromTyped(inputs), inputs,
				inferType(children), inferColumns(children), children);
		Preconditions.checkArgument(children.size() > 1);
		assert isPredicateConsistent(predicate, this.columns);
		this.relativeInputPositions = ImmutableMap.copyOf(inferInputMappings(inputs, children));
		this.predicate = predicate;
	}
	
	/**
	 * Checks if is predicate consistent.
	 *
	 * @param predicate the predicate
	 * @param columns the columns
	 * @return true if the predicate if position-consistent with columns.
	 */
	private static boolean isPredicateConsistent(Condition predicate, List<Typed> columns) {
		if (predicate instanceof ConjunctiveCondition) {
			for (SimpleCondition subPred: ((ConjunctiveCondition) predicate).getSimpleConditions()) {
				if (!isPredicateConsistent(subPred, columns)) 
					return false;
			}
		}
		else if (predicate instanceof ConstantEqualityCondition) {
			int pos = ((ConstantEqualityCondition) predicate).getPosition();
			if (pos < 0 || pos >= columns.size()) {
				return false;
			}
		}
		else if (predicate instanceof AttributeEqualityCondition) {
			int pos = ((AttributeEqualityCondition) predicate).getPosition();
			if (pos < 0 || pos >= columns.size()) {
				return false;
			}
			if (predicate instanceof AttributeEqualityCondition) {
				int other = ((AttributeEqualityCondition) predicate).getOther();
				if (other < 0 || other >= columns.size()) {
					return false;
				}
			}
		} 
		return true;
	}
	
	/**
	 * Gets the predicate.
	 *
	 * @return the join predicate
	 */
	public Condition getPredicate() {
		return this.predicate;
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
	 * Initialises the join variables.
	 * @param children TupleIterator[]
	 * @return Atom
	 */
	protected static Condition inferNaturalJoin(Collection<TupleIterator> children) {
		Map<Typed, SortedSet<Integer>> joinVariables = new LinkedHashMap<>();
		int totalCol = 0;
		// Cluster patterns by variables
		for (TupleIterator child : children) {
			for (int i = 0; i < child.getColumns().size(); i++) {
				Typed col = child.getColumns().get(i);
				SortedSet<Integer> joined = joinVariables.get(col);
				if (joined == null) {
					joined = new TreeSet<>();
					joinVariables.put(col, joined);
				}
				joined.add(totalCol);
				totalCol++;
			}
		}
		
		Collection<AttributeEqualityCondition> equalities = new ArrayList<>();
		// Remove clusters containing only one pattern
		for (Iterator<Typed> keys = joinVariables.keySet().iterator(); keys.hasNext();) {
			Set<Integer> cluster = joinVariables.get(keys.next());
			if (cluster.size() < 2) {
				keys.remove();
			} else {
				Iterator<Integer> i = cluster.iterator();
				Integer left = i.next();
				while (i.hasNext()) {
					Integer right = i.next();
					equalities.add(AttributeEqualityCondition.create(left, right));
				}
			}
		}

		return ConjunctiveCondition.create(equalities);
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.NaryIterator#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName());
		result.append(this.predicate).append('(');
		if (this.children != null) {
			for (TupleIterator child: this.children) {
				result.append(child.toString()).append(',');
			}
			result.deleteCharAt(result.length() - 1);
		}
		result.append(')');
		return result.toString();
	}

	/**
	 * Move the iterator forward and prepares the next tuple to be returned.
	 */
	protected abstract void nextTuple() ;

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
		if (this.eventBus != null) {
			this.eventBus.post(this);
		}
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		Tuple result = this.nextTuple;
		this.nextTuple = null;
		if ((!this.hasNext() && result == null) || this.isEmpty) {
			throw new NoSuchElementException("End of operator reached.");
		}
		return result;
	}

	
	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.NaryIterator#bind(uk.ac.ox.cs.pdq.util.Tuple)
	 */
	@Override
	public void bind(Tuple t) {
		super.bind(t);
		this.nextTuple();
	}
}