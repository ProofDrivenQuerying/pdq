// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.legacy.services.policies;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.datasources.legacy.services.ServiceRepository;

/**
 * Factory for usage policies.
 * Assume all the relevant class have a constructor taking a single Properties
 * instance as unique argument
 * @author Julien Leblay
 *
 */
public class PolicyFactory {

	/** Logger. */
	private static Logger log = Logger.getLogger(PolicyFactory.class);

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
					"Could not instantiation usage policy '" + cl + "'");
		}
	}

	/**
	 * Gets the single instance of PolicyFactory.
	 *
	 * @param repo ServiceRepository
	 * @param cl the cl
	 * @param properties the properties
	 * @return a instance of the given usage policy class, initialised with the
	 * given properties.
	 */
	public static UsagePolicy getInstance(ServiceRepository repo, Class<UsagePolicy> cl, Properties properties) {
		try {
			return cl.getConstructor(ServiceRepository.class, Properties.class).newInstance(repo, properties);
		} catch (NoSuchMethodException e) {
			log.debug(e);
			return getInstance(cl, properties);
		} catch (InvocationTargetException
				| IllegalAccessException
				| InstantiationException e) {
			throw new IllegalArgumentException(
					"Could not instantiation usage policy '" + cl + "'", e);
		}
	}
}
