package uk.ac.ox.cs.pdq.datasources.services.policies;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * Factory for usage policies.
 * Assume all the relevant class have a constructor taking a single Properties
 * instance as unique argument
 * @author Julien Leblay
 *
 */
public class PolicyFactory {

	/**
	 * Gets the single instance of PolicyFactory.
	 *
	 * @param cl the cl
	 * @param properties the properties
	 * @return a instance of the given usage policy class, initialized with the
	 * given properties.
	 */
	public static UsagePolicy getInstance(Class<UsagePolicy> cl, Properties properties) {
		try {
			return cl.getConstructor(Properties.class).newInstance(properties);
		} catch (NoSuchMethodException
				| InvocationTargetException
				| IllegalAccessException
				| InstantiationException e) {
			throw new IllegalArgumentException(
					"Could not instantiate usage policy '" + cl + "'");
		}
	}
}
