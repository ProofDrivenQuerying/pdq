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

import uk.ac.ox.cs.pdq.algebra.predicates.AttributeEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.EqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.Predicate;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * Join is a top-level class for all join implementations.
 * 
 * @author Julien Leblay
 */
public abstract class Join extends NaryIterator {

	protected Predicate predicate;
	
	/** Determines whether the operator is known to have an empty result. */
	protected boolean isEmpty = false;
	
	/** The next tuple to return. */
	protected Tuple nextTuple = null;

	/**
	 * Instantiates a new join.
	 * 
	 * @param children
	 *            the children
	 * @param inputs List<Typed>
	 */
	protected Join(List<Typed> inputs, TupleIterator... children) {
		this(inferNaturalJoin(Lists.newArrayList(children)), inputs, Lists.newArrayList(children));
	}
	
	/**
	 * Instantiates a new join.
	 * 
	 * @param children
	 *            the children
	 * @param inputs List<Typed>
	 */
	protected Join(List<Typed> inputs, List<TupleIterator> children) {
		this(inferNaturalJoin(children), inputs, children);
	}
	
	/**
	 * Instantiates a new join.
	 * @param predicate Predicate
	 * @param inputs List<Typed>
	 * @param children
	 *            the children
	 */
	protected Join(Predicate predicate, List<Typed> inputs, Collection<TupleIterator> children) {
		super(TupleType.DefaultFactory.createFromTyped(inputs), inputs,
				inferType(children), inferColumns(children), children);
		Preconditions.checkArgument(children.size() > 1);
		assert isPredicateConsistent(predicate, this.columns);
		this.relativeInputPositions = ImmutableMap.copyOf(inferInputMappings(inputs, children));
		this.predicate = predicate;
	}
	
	/**
	 * 
	 * @param predicate
	 * @param columns
	 * @return true if the predicate if position-consistent with columns.
	 */
	private static boolean isPredicateConsistent(Predicate predicate, List<Typed> columns) {
		if (predicate instanceof ConjunctivePredicate) {
			ConjunctivePredicate<Predicate> conjunction = ((ConjunctivePredicate) predicate);
			for (Predicate subPred: conjunction) {
				if (!isPredicateConsistent(subPred, columns)) {
					return false;
				}
			}
			
		}
		if (predicate instanceof EqualityPredicate) {
			int pos = ((EqualityPredicate) predicate).getPosition();
			if (pos < 0 || pos >= columns.size()) {
				return false;
			}
			if (predicate instanceof AttributeEqualityPredicate) {
				int other = ((AttributeEqualityPredicate) predicate).getOther();
				if (other < 0 || other >= columns.size()) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * @return the join predicate
	 */
	public Predicate getPredicate() {
		return this.predicate;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#open()
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
	 * @return Predicate
	 */
	protected static Predicate inferNaturalJoin(Collection<TupleIterator> children) {
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
		
		Collection<AttributeEqualityPredicate> equalities = new ArrayList<>();
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
					equalities.add(new AttributeEqualityPredicate(left, right));
				}
			}
		}

		return new ConjunctivePredicate<>(equalities);
	}

	/*
	 * (non-Javadoc)
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
	 * 
	 */
	protected abstract void nextTuple() ;

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#hasNext()
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

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#next()
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

	
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#bind(Tuple)
	 */
	@Override
	public void bind(Tuple t) {
		super.bind(t);
		this.nextTuple();
	}
}