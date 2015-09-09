package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.List;

import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;


/**
 * Superclass to all unary physical operator.
 * 
 * @author Julien LEBLAY
 */
public abstract class UnaryIterator extends TupleIterator {

	/** The sole child of the operator */
	protected TupleIterator child = null;
	
	/**
	 * Instantiates a new operator.
	 * 
	 * @param child TupleIterator
	 */
	public UnaryIterator(TupleIterator child) {
		this(outputColumns(child), child);
	}
	
	/**
	 * Instantiates a new operator.
	 * 
	 * @param typeOverride TupleType
	 * @param child TupleIterator
	 */
	protected UnaryIterator(TupleType typeOverride, TupleIterator child) {
		this(inputColumns(child), typeOverride, outputColumns(child), child);
	}
	
	/**
	 * Instantiates a new operator.
	 * 
	 * @param columns List<Typed>
	 * @param child TupleIterator
	 */
	protected UnaryIterator(List<Typed> columns, TupleIterator child) {
		this(inputColumns(child), TupleType.DefaultFactory.createFromTyped(columns), columns, child);
	}
	
	/**
	 * Instantiates a new operator.
	 * 
	 * @param typeOverride TupleType
	 * @param columns List<Typed>
	 * @param child TupleIterator
	 */
	protected UnaryIterator(List<Typed> inputs, TupleType typeOverride, List<Typed> columns, TupleIterator child) {
		super(TupleType.DefaultFactory.createFromTyped(inputs), inputs,
				typeOverride, Lists.newArrayList(columns));
		Preconditions.checkArgument(child != null);
		this.child = child;
	}
	
	/**
	 * Gets the child.
	 * 
	 * @return the child
	 */
	public TupleIterator getChild() {
		return this.child;
	}
	
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#setEventBus(com.google.common.eventbus.EventBus)
	 */
	@Override
	public void setEventBus(EventBus eb) {
		super.setEventBus(eb);
		this.child.setEventBus(eb);
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#open()
	 */
	@Override
	public void open() {
		Preconditions.checkState(this.open == null || this.open);
		this.child.open();
		this.open = true;
	}

	/*
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() {
		Preconditions.checkState(this.open != null && this.open);
		super.close();
		this.child.close();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#reset()
	 */
	@Override
	public void reset() {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkState(!this.interrupted);
		this.child.reset();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator#interrupt()
	 */
	@Override
	public void interrupt() {
		Preconditions.checkState(this.open != null && this.open);
		this.interrupted = true;
		this.child.interrupt();
	}
	
	/**
	 * @param t Tuple
	 */
	@Override
	public void bind(Tuple t) {
		Preconditions.checkState(this.open != null && this.open);
		Preconditions.checkArgument(t != null);
		Preconditions.checkArgument(t.getType().equals(this.inputType));
		this.child.bind(t);
	}
}
