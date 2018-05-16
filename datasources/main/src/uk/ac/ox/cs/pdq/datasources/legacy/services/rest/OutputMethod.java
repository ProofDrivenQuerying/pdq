package uk.ac.ox.cs.pdq.datasources.legacy.services.rest;

import java.util.Map;

/**
 * Interface to output method, i.e. classes in charges are extract attribute
 * values from JSON results.
 * 
 * @author Julien Leblay
 */
public interface OutputMethod {

	/**
	 * Extract an attribute's value from a JSON result mapped into a Map<String, Object>.
	 *
	 * @param wrapper the wrapper
	 * @return Object
	 */
	public Object extract(Map<String, Object> wrapper);
}
