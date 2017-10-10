package uk.ac.ox.cs.pdq.datasources.services.rest;

import java.util.Collection;

import uk.ac.ox.cs.pdq.db.Attribute;

/**
 * A static attribute is an attribute whose value does not change at
 * runtime, and is not consider an actual attribute of relation.
 * However, it share some of its behaviour with RESTAttributes, in
 * particular in the way input method are used. 
 *
 * @author Julien Leblay
 * @param <T> the generic type
 */
public class RESTStaticInput<T> extends RESTAttribute implements StaticInput<T> {
	private static final long serialVersionUID = 454377200312328329L;
	/**  The input's default value. */
	private final T defaultValue;

	/**
	 * Constructor for RESTStaticInput.
	 * @param attribute Attribute
	 * @param inputMethod InputMethod
	 */
	public RESTStaticInput(Attribute attribute, InputMethod inputMethod) {
		this(attribute, inputMethod, null, null);
	}
	
	/**
	 * Constructor for RESTStaticInput.
	 * @param attribute Attribute
	 * @param inputMethod InputMethod
	 * @param inputParams Collection<String>
	 */
	public RESTStaticInput(Attribute attribute, InputMethod inputMethod, Collection<String> inputParams) {
		this(attribute, inputMethod, inputParams, null);
	}
	
	/**
	 * Constructor for RESTStaticInput.
	 * @param attribute Attribute
	 * @param inputMethod InputMethod
	 * @param defaultValue T
	 */
	public RESTStaticInput(Attribute attribute, InputMethod inputMethod, T defaultValue) {
		this(attribute, inputMethod, null, defaultValue);
	}
	
	/**
	 * Constructor for RESTStaticInput.
	 * @param attribute Attribute
	 * @param inputMethod InputMethod
	 * @param inputParams Collection<String>
	 * @param defaultValue T
	 */
	public RESTStaticInput(Attribute attribute, InputMethod inputMethod, Collection<String> inputParams, T defaultValue) {
		super(attribute, null, inputMethod, inputParams);
		this.defaultValue = defaultValue;
	}

	/**
	 * Gets the default value.
	 *
	 * @return T
	 * @see uk.ac.ox.cs.pdq.datasources.services.rest.StaticInput#getDefaultValue()
	 */
	@Override
	public T getDefaultValue() {
		return this.defaultValue;
	}
}
