package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.wrappers.RelationAccessWrapper;
import uk.ac.ox.cs.pdq.util.ResetableIterator;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Access over a relation, where the input are provided by the child operator.
 * Julien: although this class is only marginally used, please do not delete.
 * Planning to use it in future work.
 * 
 * @author Julien Leblay
 */
public class BottomUpAccess extends UnaryIterator {


	/** The input table of the access. */
	private final RelationAccessWrapper relation;
	
	/** The access method to use. */
	private final AccessMethod accessMethod;
	
	/** Iterator over the output tuples. */
	protected ResetableIterator<Tuple> outputs = null;
	
	/**
	 * Instantiates a new join.
	 * 
	 * @param relation RelationAccessWrapper
	 * @param mt AccessMethod
	 * @param child TupleIterator
	 */
	public BottomUpAccess(RelationAccessWrapper relation, AccessMethod mt, TupleIterator child) {
		super(attributes(relation), child);
		Preconditions.checkArgument(mt != null);
		Preconditions.checkArgument(mt.getType() != Types.FREE);
		Preconditions.checkArgument(relation.getAccessMethod(mt.getName()) != null);
		Preconditions.checkArgument(child.getType().equals(
				TupleType.DefaultFactory.createFromTyped(relation.getInputAttributes(mt))));
		this.relation = relation;
		this.accessMethod = mt;
	}
	
	private static List<Typed> attributes(RelationAccessWrapper relation) {
		Preconditions.checkArgument(relation != null);
		return Lists.<Typed>newArrayList(relation.getAttributes());
	}

	/**
	 * @return the relation being accessed
	 */
	public RelationAccessWrapper getRelation() {
		return this.relation;
	}

	/**
	 * @return the access method being used
	 */
	public AccessMethod getAccessMethod() {
		return this.accessMethod;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getSimpleName());
		result.append('[').append(this.relation.getName()).append('/');
		result.append(this.accessMethod).append(']');
		result.append('(').append(this.child).append(')');
		return result.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.UnaryIterator#open()
	 */
	@Override
	public void open() {
		super.open();
		this.outputs = null;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.UnaryIterator#reset()
	 */
	@Override
	public void reset() {
		super.reset();
		this.outputs = null;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.UnaryIterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		if (this.outputs == null) {
			this.init();
		}
		return this.outputs.hasNext();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#next()
	 */
	@Override
	public Tuple next() {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		if (this.outputs == null) {
			this.init();
		}
		return this.outputs.next();
	}

	/**
	 * Initializes the operator by performing the access and preloading the
	 * results into iteratorCache.
	 */
	private void init() {
		List<Attribute> inputAttributes = new ArrayList<>();
		List<Typed> outputAttributes = this.getColumns();
		for (Integer i: this.accessMethod.getInputs()) {
			Typed att = outputAttributes.get(i - 1);
			Preconditions.checkState(att instanceof Attribute);
			inputAttributes.add((Attribute) att);
		}
		this.outputs = this.relation.iterator(inputAttributes, this.child);
		this.outputs.open();
	}
	
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#deepCopy()
	 */
	@Override
	public BottomUpAccess deepCopy() {
		return new BottomUpAccess(this.relation, this.accessMethod, this.child.deepCopy());
	}
}