// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.legacy.services.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.base.Joiner;

/**
 * This class capture the behavior have incorporate inputs parameters to a 
 * request. Input are (currently) one of two types: URL parameter or path
 * element.
 * 
 * @author Julien Leblay
 */
public class InputMethod {

	/** Logger. */
	private static Logger log = Logger.getLogger(InputMethod.class);

	/**
	 *  Allowed types.
	 */
	public static enum Types {
		URL_PARAM, 
		PATH_ELEMENT}

	/** Separator for parameters. */
	private static final String PARAM_SEPARATOR = ".";

	/**  The input method name. */
	private final String name;

	/**  The input method type. */
	private final Types type;

	/** 
	 *  a template  to build value from parameters. Parameter are placed
	 * in the template in the form of {str}, where str can be any string.
	 */
	private final String template;

	/**  If batch input is allowed, input should be separated with this string. */
	private final String batchDelimiter;

	/** If batch input is allowed, this defines how many input are allowed in a single batch. */
	private final int batchSize;

	/** If a default value is define, it is used when no value is provided at run time. */
	private final Object defaultValue;

	/**
	 * Default constructor.
	 *
	 * @param name the name
	 * @param template the template
	 * @param type the type
	 * @param batchDelimiter the batch delimiter
	 * @param batchSize the batch size
	 * @param defaultValue the default value
	 */
	public InputMethod(String name, String template, Types type, String batchDelimiter, Integer batchSize, Object defaultValue) {
		super();
		this.name = name;
		this.type = type;
		this.template = template;
		this.batchDelimiter = batchDelimiter;
		this.batchSize = batchSize == null ? Integer.MAX_VALUE : batchSize;
		this.defaultValue = defaultValue;
	}

	/**
	 * Empty constructor, uses all possible default values.
	 *
	 * @param name the name
	 */
	public InputMethod(String name) {
		this(name, null, Types.URL_PARAM, null, null, null);
	}

	/**
	 *
	 * @param key the key
	 * @return a string made of the input method's name parameter with key
	 */
	public String compoundKey(String key) {
		return this.name + PARAM_SEPARATOR + key;
	}

	/**
	 *
	 * @param params the params
	 * @return a string copies from the template, where the proper fields
	 * have been replaced with value provided in params (default value otherwise).
	 */
	public String format(Map<String, Object> params) {
		StringBuilder result = new StringBuilder();
		String sep = "";
		if (this.template != null) {
			result.append(sep).append(this.applyTemplate(params));
		} else {
			Object value = params.get(this.name);
			if (value == null) {
				value = this.defaultValue;
			}
			result.append(value);
		}
		return result.toString();
	}

	/**
	 *
	 * @param params the params
	 * @return a string copies from the template, where the proper fields
	 * have been replaced with value provided in params.
	 */
	private String applyTemplate(Map<String, Object> params) {
		String result = this.template;
		try {
			for (String key: params.keySet()) {
				if (key.startsWith(this.name)) {
					String localKey = key.replace(this.name + PARAM_SEPARATOR, "");
					if (localKey.trim().isEmpty() || !result.contains("{" + localKey + "}")) {
						log.warn("Key {" + localKey + "} not found in template " + result);
					}
					result = result.replace("{" + localKey + "}", URLEncoder.encode(String.valueOf(params.get(key)), "UTF-8"));
				}
			}
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException();
		}
		return result.toString();
	}

	/**
	 *
	 * @return the name of this input method
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * redundant with compound key. will be removed.
	 *
	 * @param params the params
	 * @return String
	 */
	public String getParameterizedName(Collection<String> params) {
		return this.name + PARAM_SEPARATOR + Joiner.on(PARAM_SEPARATOR).join(params);
	}

	/**
	 *
	 * @return the input method type.
	 */
	public Types getType() {
		return this.type;
	}

	/**
	 * Gets the default value.
	 *
	 * @return the input method's default value
	 */
	public Object getDefaultValue() {
		return this.defaultValue;
	}

	/**
	 *
	 * @return the batch delimiter
	 */
	public String getBatchDelimiter() {
		return this.batchDelimiter;
	}

	/**
	 *
	 * @return the batch size.
	 */
	public Integer getBatchSize() {
		return this.batchSize;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("InputMethod(")
		.append(this.name).append(',')
		.append(this.type).append(',')
		.append(this.template).append(',')
		.append(this.defaultValue).append(',')
		.append(this.batchDelimiter).append(')');
		return result.toString();
	}
}
