package uk.ac.ox.cs.pdq.runtime.exec.iterator;

import org.junit.Assert;

import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.datasources.ResetableIterator;
import uk.ac.ox.cs.pdq.datasources.utility.Tuple;
import uk.ac.ox.cs.pdq.db.Attribute;


// TODO: Auto-generated Javadoc
/**
 * TupleIterator defines a top-level class for all physical operators.
 * 
 * @author Julien Leblay
 */
public abstract class TupleIterator  implements AutoCloseable, ResetableIterator<Tuple> {		
	
	protected final Attribute[] inputAttributes;
	
	protected final Attribute[] outputAttributes;
	
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
	 * @param inputType TupleType
	 * @param inputColumns List<Typed>
	 * @param outputAttributes TupleType
	 * @param outputColumns List<Typed>
	 */
	public TupleIterator(Attribute[] inputAttributes, Attribute[] outputAttributes) {
		Assert.assertNotNull(inputAttributes != null);
		Assert.assertNotNull(outputAttributes != null);
		this.inputAttributes = inputAttributes;
		this.outputAttributes = outputAttributes;
	}

	/**
	 * Gets the input tuple type.
	 * 
	 * @return the input type
	 */
	public Attribute[] getInputAttributes() {
		return this.inputAttributes.clone();
	}
	
	/**
	 * Gets the input tuple type.
	 * 
	 * @return the input type
	 */
	public Attribute[] getOutputAttributes() {
		return this.outputAttributes.clone();
	}
	
	public Attribute getOutputAttribute(int index) {
		return this.outputAttributes[index];
	}
	
	public Attribute getInputAttribute(int index) {
		return this.inputAttributes[index];
	}
	
	public Integer getNumberOfOutputAttributes() {
		return this.outputAttributes.length;
	}
	
	public Integer getNumberOfInputAttributes() {
		return this.inputAttributes.length;
	}
	
	public abstract TupleIterator[] getChildren();
	
	public abstract TupleIterator getChild(int childIndex);
		
	/**
	 * Gets the columns display.
	 *
	 * @return a list of human readable column headers.
	 */
	public String[] getColumnsDisplay() {
		String[] result = new String[this.outputAttributes.length];
		for(int index = 0; index < this.outputAttributes.length; ++index)
			result[index] = this.outputAttributes[index].getType().toString();
		return result;
	}
	
	/**
	 * Sets the given event bus to this operator and all its children. 
	 * @param eb EventBus
	 */ 
	public void setEventBus(EventBus eb) {
		this.eventBus = eb;
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
	 * Closes the operator. This method throws an exception if called when 
	 * already closed. 
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() {
		Assert.assertTrue(this.open != null && this.open);
		this.open = false;
	}
	
	/**
	 * Interrupted the operator. This method throws an exception if called when 
	 * already closed.
	 */
	public abstract void interrupt();
	
	/**
	 * Bind.
	 *
	 * @param tuple Tuple
	 */
	public abstract void bind(Tuple tuple);
}
