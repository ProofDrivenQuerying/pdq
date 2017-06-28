package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.NoSuchElementException;

import uk.ac.ox.cs.pdq.algebra.predicates.Predicate;
import uk.ac.ox.cs.pdq.datasources.ResetableIterator;
import uk.ac.ox.cs.pdq.datasources.memory.RelationAccessWrapper;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;


// TODO: Auto-generated Javadoc
/**
 * Scan over a relation
 * Julien: although this class is only marginally used, please do not delete.
 * Planning to use it in future work.
 * 
 * @author Julien Leblay
 */
public class Scan extends TupleIterator {

	/** The next tuple to return. */
	protected ResetableIterator<Tuple> tupleIterator = null;

	/** The underlying relation. */
	protected RelationAccessWrapper relation = null;	

	/**  The next tuple to return. */
	private Tuple nextTuple;
	
	/** The filter. */
	protected final Predicate filter;	

	/**
	 * Instantiates a new join.
	 * 
	 * @param relation RelationAccessWrapper
	 * @param filter additional filtering condition
	 */
	public Scan(RelationAccessWrapper relation, Predicate filter) {
		super(Lists.<Typed>newArrayList(), 
				Lists.<Typed>newArrayList(relation.getAttributes()));
		this.relation = relation;
		this.filter = filter;
	}

	/**
	 * Instantiates a new join.
	 * 
	 * @param relation RelationAccessWrapper
	 */
	public Scan(RelationAccessWrapper relation) {
		this(relation, null);
	}

	/**
	 * Gets the relation.
	 *
	 * @return the relation being scanned
	 */
	public RelationAccessWrapper getRelation() {
		return this.relation;
	}

	/**
	 * Gets the filter.
	 *
	 * @return the filter for this scan if any
	 */
	public Predicate getFilter() {
		return this.filter;
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName());
		if (this.filter != null) {
			result.append("[#").append(filter).append(']');
		}
		if (this.relation != null) {
			result.append('(').append(this.relation.getName()).append(')');
		}
		return result.toString();
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.datasources.ResetableIterator#open()
	 */
	@Override
	public void open() {
		Preconditions.checkState(this.open == null || this.open);
		this.open = true;
		this.tupleIterator = this.relation.iterator();
		this.tupleIterator.open();
		this.nextTuple();
	}

	/**
	 * Moves to the next valid tuple to return.
	 */
	private void nextTuple() {
		while (this.tupleIterator.hasNext()) {
			this.nextTuple = this.tupleIterator.next();
			if (this.filter == null || this.filter.isSatisfied(this.nextTuple)) {
				return;
			}
		}
		this.nextTuple = null;
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#close()
	 */
	@Override
	public void close() {
		Preconditions.checkState(this.open != null && this.open);
		super.close();
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.datasources.ResetableIterator#reset()
	 */
	@Override
	public void reset() {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		this.tupleIterator.reset();
		this.nextTuple();
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		Preconditions.checkState(this.open != null && this.open);
		return !this.interrupted && this.nextTuple != null;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Tuple next() {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		if (this.eventBus != null) {
			this.eventBus.post(this);
		}
		if (this.nextTuple == null) {
			throw new NoSuchElementException();
		}
		Tuple result = this.nextTuple;
		this.nextTuple();
		return result;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#deepCopy()
	 */
	@Override
	public Scan deepCopy() {
		return new Scan(this.relation, this.filter);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#interrupt()
	 */
	@Override
	public void interrupt() {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		this.interrupted = true;
	}

	/**
	 * Bind.
	 *
	 * @param t Tuple
	 */
	@Override
	public void bind(Tuple t) {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		Preconditions.checkArgument(t != null);
		Preconditions.checkArgument(t.size() == 0);
		// Important: this iterator MUST be reiterated from scratch
		this.reset();
	}
}