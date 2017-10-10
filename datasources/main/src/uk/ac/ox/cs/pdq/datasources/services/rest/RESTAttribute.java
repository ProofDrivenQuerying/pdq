package uk.ac.ox.cs.pdq.datasources.services.rest;

import java.util.Collection;

import uk.ac.ox.cs.pdq.db.Attribute;

/**
 * A REST attribute is an attribute whose behavior includes ways to produce 
 * inputs on a service access.
 *  
 * @author Julien Leblay
 */
public class RESTAttribute extends Attribute {
	
	private static final long serialVersionUID = -4628829110700152694L;

	/**  The input method of this attribute. */
	private final InputMethod inputMethod;
	
	/**  The output method of this attribute. */
	private final OutputMethod outputMethod;
	
	/**  The parameters associated with this input method. */
	private final Collection<String> inputParams;
	
	/**
	 * Constructor for RESTAttribute.
	 * @param attribute Attribute
	 * @param inputMethod InputMethod
	 */
	public RESTAttribute(Attribute attribute, InputMethod inputMethod) {
		this(attribute, inputMethod, null);
	}
	
	/**
	 * Constructor for RESTAttribute.
	 * @param attribute Attribute
	 * @param inputMethod InputMethod
	 * @param inputParams Collection<String>
	 */
	public RESTAttribute(Attribute attribute, InputMethod inputMethod, Collection<String> inputParams) {
		this(attribute, null, inputMethod, inputParams);
	}
	
	/**
	 * Constructor for RESTAttribute.
	 * @param attribute Attribute
	 * @param outputMethod OutputMethod
	 */
	public RESTAttribute(Attribute attribute, OutputMethod outputMethod) {
		this(attribute, outputMethod, null, null);
	}
	
	/**
	 * Constructor for RESTAttribute.
	 * @param attribute Attribute
	 * @param outputMethod OutputMethod
	 * @param inputMethod InputMethod
	 */
	public RESTAttribute(Attribute attribute, OutputMethod outputMethod, InputMethod inputMethod) {
		this(attribute, outputMethod, inputMethod, null);
	}

	/**
	 * Constructor for RESTAttribute.
	 * @param attribute Attribute
	 * @param outputMethod OutputMethod
	 * @param inputMethod InputMethod
	 * @param inputParams Collection<String>
	 */
	public RESTAttribute(Attribute attribute, OutputMethod outputMethod, InputMethod inputMethod, Collection<String> inputParams) {
		super(attribute.getType(), attribute.getName());
		this.inputMethod = inputMethod;
		this.outputMethod = outputMethod;
		this.inputParams = inputParams;
	}
	
	/**
	 * Gets the input method.
	 *
	 * @return InputMethod
	 */
	public InputMethod getInputMethod() {
		return this.inputMethod;
	}

	/**
	 * Gets the output method.
	 *
	 * @return OutputMethod
	 */
	public OutputMethod getOutputMethod() {
		return this.outputMethod;
	}

	/**
	 * Gets the input params.
	 *
	 * @return Collection<String>
	 */
	public Collection<String> getInputParams() {
		return this.inputParams;
	}

	/**
	 * Allows batch.
	 *
	 * @return if the attribute's input method allows batch input, i.e. has
	 * a batch delimiter defined, and an output method.
	 */
	public boolean allowsBatch() {
		return this.inputMethod != null
				&& this.outputMethod != null
				&& this.inputMethod.getBatchDelimiter() != null;
	}
}
