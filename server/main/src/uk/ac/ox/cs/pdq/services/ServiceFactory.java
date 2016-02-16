package uk.ac.ox.cs.pdq.services;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * Produces services based of their name and their implementation given in 
 * registered classes.
 *  
 * @author Julien Leblay
 *
 */
public class ServiceFactory {
	
	/**  Registered service implementations, indexed by name. */
	private static Map<String, Class<? extends Service>>
			registeredNamedServices = new LinkedHashMap<>();

	/**
	 * Registers a new service implementation under the given name.
	 *
	 * @param name the name
	 * @param service the service
	 */
	public static void register(String name, String service) {
		assert name != null;
		assert service != null;
		try {
			Class<? extends Service> serviceClass = 
					(Class<? extends Service>) Class.forName(service);
			registeredNamedServices.put(name, serviceClass);
		} catch (ClassCastException | ClassNotFoundException e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * Checks if is registered.
	 *
	 * @param name the name
	 * @return true if the service by the given name is currently registered.
	 */
	public static boolean isRegistered(String name) {
		assert name != null;
		return registeredNamedServices.containsKey(name);
	}
	
	/**
	 * Instantiates a service registered under the given name. 
	 *
	 * @param configDir the config dir
	 * @param name the name
	 * @return Service
	 */
	public static Service create(File configDir, String name) {
		assert name != null;
		final Class<? extends Service> serviceClass = registeredNamedServices.get(name);
		if (serviceClass == null) {
			throw new ServiceException("'" + name + "' does not corresponding to any registered service");
		}
		try {
			return serviceClass.getConstructor(File.class).newInstance(configDir);
		} catch (IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException
				| InstantiationException | IllegalAccessException e) {
			throw new ServiceException("Unable to start service due to prior exception.", e);
		}
	}
}
