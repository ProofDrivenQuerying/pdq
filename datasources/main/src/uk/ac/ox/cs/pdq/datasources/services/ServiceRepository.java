package uk.ac.ox.cs.pdq.datasources.services;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.datasources.services.policies.PolicyFactory;
import uk.ac.ox.cs.pdq.datasources.services.policies.UsagePolicy;
import uk.ac.ox.cs.pdq.datasources.services.rest.InputMethod;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * A collection of Services, with shared usage policies and input methods.
 * 
 * @author Julien Leblay
 */
public class ServiceRepository {

	/**
	 * Map from service's names to their instances.
	 */
	private final Map<String, Service> services = new LinkedHashMap<>();

	/**
	 * Map from usage policies's names to their instances.
	 */
	private final Map<String, UsagePolicy> usagePolicies = new LinkedHashMap<>();

	/**
	 * Map from input methods's names to their instances.
	 */
	private final Map<String, InputMethod> inputMethods = new LinkedHashMap<>();

	/**
	 * Default constructor.
	 */
	public ServiceRepository() {}
	
	/**
	 * Registers a service.
	 *
	 * @param name the name
	 * @param service the service
	 */
	public void registerService(String name, Service service) {
		Preconditions.checkArgument(name != null);
		Preconditions.checkArgument(service != null);
		this.services.put(name, service);
	}
	
	/**
	 * Unregisters a service.
	 *
	 * @param name the name
	 */
	public void unregisterService(String name) {
		Preconditions.checkArgument(name != null);
		this.services.remove(name);
	}
	
	/**
	 * Registers a common usage policy.
	 *
	 * @param name the name
	 * @param policy UsagePolicy
	 */
	public void registerUsagePolicy(String name, UsagePolicy policy) {
		Preconditions.checkArgument(name != null);
		Preconditions.checkArgument(policy != null);
		this.usagePolicies.put(name, policy);
	}
	
	/**
	 * Registers a common usage policy.
	 *
	 * @param name the name
	 * @param cl Class<UsagePolicy>
	 * @param properties Properties
	 */
	public void registerUsagePolicy(String name, Class<UsagePolicy> cl, Properties properties) {
		Preconditions.checkArgument(name != null);
		Preconditions.checkArgument(cl != null);
		Preconditions.checkArgument(properties != null);
		this.usagePolicies.put(name, PolicyFactory.getInstance(this, cl, properties));
	}
	
	/**
	 * Registers a common input method.
	 *
	 * @param name the name
	 * @param method InputMethod
	 */
	public void registerInputMethod(String name, InputMethod method) {
		Preconditions.checkArgument(name != null);
		Preconditions.checkArgument(method != null);
		this.inputMethods.put(name, method);
	}
	
	/**
	 * Unregisters a common input method.
	 *
	 * @param name the name
	 */
	public void unregisterInputMethod(String name) {
		Preconditions.checkArgument(name != null);
		this.inputMethods.remove(name);
	}

	/**
	 * Resolves a parameterised input method, and a returns a pair contained 
	 * that method and its parameters in an array of Strings.
	 *
	 * @param att the att
	 * @return Pair<InputMethod,String[]>
	 */
	public Pair<InputMethod, String[]> parseInputMethod(String att) {
		if (att == null) {
			return null;
		}
		InputMethod im = null;
		String[] params = null;
		String[] methodParam = att.split("\\.");
		
		im = this.inputMethods.get(methodParam[0]);
		if (im == null) {
			return null;
		}
		if (methodParam.length > 1) {
			params = new String[methodParam.length - 1];
			for (int i = 1, l = methodParam.length; i < l; i++) {
				params[i - 1] = methodParam[i];
			}
		}
		return Pair.of(im, params);
	}

	/**
	 * Gets the service.
	 *
	 * @param name the name
	 * @return the service by the given name
	 */
	public Service getService(String name) {
		Preconditions.checkArgument(name != null);
		return this.services.get(name);
	}
	
	/**
	 * Gets the usage policy.
	 *
	 * @param name the name
	 * @return the usage policy by the given name
	 */
	public UsagePolicy getUsagePolicy(String name) {
		Preconditions.checkArgument(name != null);
		return this.usagePolicies.get(name);
	}
	
	/**
	 * Gets the input method.
	 *
	 * @param name the name
	 * @return the input method by the given name
	 */
	public InputMethod getInputMethod(String name) {
		Preconditions.checkArgument(name != null);
		Pair<InputMethod, String[]> result = this.parseInputMethod(name);
		if (result == null) {
			return null;
		}
		return result.getLeft();
	}

	/**
	 * Gets the services.
	 *
	 * @return a Collection of all the service in the repository.
	 */
	public Collection<Service> getServices() {
		return this.services.values();
	}
}
