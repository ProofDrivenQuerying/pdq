package uk.ac.ox.cs.pdq.datasources.services.policies;

import java.lang.reflect.InvocationTargetException;

import uk.ac.ox.cs.pdq.datasources.io.jaxb.servicegroup.GroupUsagePolicy;

/**
 * Factory for usage policies.
 * Assume all the relevant class have a constructor taking a single Properties
 * instance as unique argument
 * @author Julien Leblay
 *
 */
public class PolicyFactory {

	public static UsagePolicy getInstance(Class<UsagePolicy> cl, GroupUsagePolicy gup) {
		try {
			return cl.getConstructor(GroupUsagePolicy.class).newInstance(gup);
		} catch (NoSuchMethodException
				| InvocationTargetException
				| IllegalAccessException
				| InstantiationException e) {
			throw new IllegalArgumentException(
					"Could not instantiate usage policy '" + cl + "'");
		}
	}
}