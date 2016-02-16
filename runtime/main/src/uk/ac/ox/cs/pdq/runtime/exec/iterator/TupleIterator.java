package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.util.ResetableIterator;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;


// TODO: Auto-generated Javadoc
/**
 * TupleIterator defines a top-level class for all physical operators.
 * 
 * @author Julien Leblay
 */
public abstract class TupleIterator  implements AutoCloseable, ResetableIterator<Tuple> {

	/** The columns. */
	protected final List<Typed> columns;
	
	/** The iterator's type. */
	protected final TupleType outputType;
	
	/** The iterator's input columns. */
	protected final List<Typed> inputColumns;
	
	/** The iterator's input type. */
	protected final TupleType inputType;
	
	/**  Tells whether the operator was voluntarily interrupted. */
	protected boolean interrupted = false;
	
	/** Tells whether the operator has been open. If null, the iterator has not
	 * been yet either opened or closed. */
	protected Boolean open = null;

	/** The event bus. */
	protected EventBus eventBus;

	/**
	 * Instantiates a new operator.
	 * 
	 * @param inputColumns List<Typed>
	 * @param outputColumns List<Typed>
	 */
	public TupleIterator(List<Typed> inputColumns, List<Typed> outputColumns) {
		this(TupleType.DefaultFactory.createFromTyped(inputColumns), 
				inputColumns, 
				TupleType.DefaultFactory.createFromTyped(outputColumns),
				outputColumns);
	}

	/**
	 * Instantiates a new operator.
	 * 
	 * @param inputType TupleType
	 * @param inputColumns List<Typed>
	 * @param outputType TupleType
	 * @param outputColumns List<Typed>
	 */
	public TupleIterator(TupleType inputType, List<Typed> inputColumns, TupleType outputType, List<Typed> outputColumns) {
		Preconditions.checkArgument(inputType != null);
		Preconditions.checkArgument(inputColumns != null);
		Preconditions.checkArgument(outputType != null);
		Preconditions.checkArgument(outputColumns != null);
		this.outputType = outputType;
		this.columns = outputColumns;
		this.inputColumns = inputColumns;
		this.inputType = inputType;
	}

	/**
	 * Output columns.
	 *
	 * @param i the i
	 * @return the list
	 */
	protected static List<Typed> outputColumns(TupleIterator i) {
		Preconditions.checkArgument(i != null);
		return i.getColumns();
	}

	/**
	 * Input columns.
	 *
	 * @param i the i
	 * @return the list
	 */
	protected static List<Typed> inputColumns(TupleIterator i) {
		Preconditions.checkArgument(i != null);
		return i.getInputColumns();
	}

	/**
	 * Output type.
	 *
	 * @param i the i
	 * @return the tuple type
	 */
	protected static TupleType outputType(TupleIterator i) {
		Preconditions.checkArgument(i != null);
		return i.getType();
	}

	/**
	 * Input type.
	 *
	 * @param i the i
	 * @return the tuple type
	 */
	protected static TupleType inputType(TupleIterator i) {
		Preconditions.checkArgument(i != null);
		return i.getType();
	}

	/**
	 * Deep copy.
	 *
	 * @return a deep copy of the operator.
	 * @see uk.ac.ox.cs.pdq.util.ResetableIterator#deepCopy()
	 */
	@Override
	public abstract TupleIterator deepCopy();

	/**
	 * Gets the input tuple type.
	 * 
	 * @return the input type
	 */
	public TupleType getInputType() {
		return this.inputType;
	}

	/**
	 * Gets the input terms.
	 * 
	 * @return the input terms
	 */
	public List<Typed> getInputColumns() {
		return this.inputColumns;
	}
	
	/**
	 * Bind.
	 *
	 * @param t Tuple
	 */
	public abstract void bind(Tuple t);
	
	/**
	 * Gets the column at index i.
	 * 
	 * @param i the index of the column to return.
	 * @return the column at index i.
	 */
	public Typed getColumn(int i) {
		return this.columns.get(i);
	}

	/**
	 * Gets the columns.
	 * 
	 * @return the columns
	 */
	public List<Typed> getColumns() {
		return this.columns;
	}

	/**
	 * Gets the tuple type.
	 * 
	 * @return the type
	 */
	public TupleType getType() {
		return this.outputType;
	}
	
	/**
	 * Sets the given event bus to this operator and all its children. 
	 * @param eb EventBus
	 */ 
	public void setEventBus(EventBus eb) {
		this.eventBus = eb;
	}

	/**
	 * Removes the.
	 *
	 * @throws UnsupportedOperationException the unsupported operation exception
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Tells whether the operator was voluntarily interrupted .
	 *
	 * @return boolean
	 */
	public boolean isInterrupted() {
		return this.interrupted;
	}
	
	/**
	 * Gets the columns display.
	 *
	 * @return a list of human readable column headers.
	 */
	public List<String> getColumnsDisplay() {
		List<String> result = new ArrayList<>();
		for (Typed t: this.getColumns()) {
			result.add(t.toString());
		}
		return result;
	}
	
	/**
	 * Closes the operator. This method throws an exception if called when 
	 * already closed. 
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() {
		Preconditions.checkState(this.open != null && this.open);
		this.open = false;
	}
	
	/**
	 * Interrupted the operator. This method throws an exception if called when 
	 * already closed.
	 */
	public abstract void interrupt();
}
