package uk.ac.ox.cs.pdq.datasources.legacy.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.junit.Assert;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import uk.ac.ox.cs.pdq.datasources.legacy.services.policies.UsagePolicy;
import uk.ac.ox.cs.pdq.datasources.legacy.services.rest.InputMethod;
import uk.ac.ox.cs.pdq.datasources.legacy.services.rest.OutputMethod;
import uk.ac.ox.cs.pdq.datasources.legacy.services.rest.RESTAttribute;
import uk.ac.ox.cs.pdq.datasources.legacy.services.rest.RESTRelation;
import uk.ac.ox.cs.pdq.datasources.legacy.services.rest.RESTStaticInput;
import uk.ac.ox.cs.pdq.builder.Builder;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.PrimaryKey;
import uk.ac.ox.cs.pdq.db.Relation;


/**
 * Builder class for online services.
 * 
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 */
public class ServiceBuilder implements Builder<Service> {

	private String name;
	
	private String protocol;

	private String url;

	private MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;

	private String resultDelimiter;

	private List<Attribute> attributes = new ArrayList<>();
	
	private List<Attribute> nonStaticAttributes = new ArrayList<>();
	
	private Map<Attribute, InputMethod> staticInputs = new LinkedHashMap<>();
	
	private Map<Attribute, Object> staticvalues = new LinkedHashMap<>();
	
	private Map<Attribute, InputMethod> inputMethods = new LinkedHashMap<>();

	private Multimap<Attribute, String> inputParams = LinkedHashMultimap.create();
	
	private Map<Attribute, OutputMethod> outputMethods = new LinkedHashMap<>();

	private List<AccessMethodDescriptor> accessMethods = new ArrayList<>();

	private Collection<UsagePolicy> policies = new ArrayList<>();
	
	private PrimaryKey primaryKey;
	
	/**
	 *
	 * @param name String
	 * @return ServiceBuilder
	 */
	public ServiceBuilder setName(String name) {
		this.name = name;
		return this;
	}

	/**
	 *
	 * @param url String
	 * @return ServiceBuilder
	 */
	public ServiceBuilder setUrl(String url) {
		this.url =  url;
		return this;
	}
	
	/**
	 *
	 * @param mediatype the mediatype
	 * @return the service builder
	 */
	public ServiceBuilder setMediaType(MediaType mediatype) {
		this.mediaType = mediatype;
		return this;
	}

	/**
	 *
	 * @param m String
	 * @return ServiceBuilder
	 */
	public ServiceBuilder setResultsDelimiter(String m) {
		this.resultDelimiter = m;
		return this;
	}

	/**
	 *
	 * @return String
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 *
	 * @param p String
	 * @return ServiceBuilder
	 */
	public ServiceBuilder setProtocol(String p) {
		this.protocol = p;
		return this;
	}

	/**
	 *
	 * @param policy UsagePolicy
	 * @return ServiceBuilder
	 */
	public ServiceBuilder addUsagePolicy(UsagePolicy policy) {
		this.policies.add(policy.copy());
		return this;
	}

	/**
	 *
	 * @param policies the policies
	 * @return ServiceBuilder
	 */
	public ServiceBuilder addUsagePolicies(Collection<UsagePolicy> policies) {
		for (UsagePolicy p: policies) {
			addUsagePolicy(p);
		}
		return this;
	}

	/**
	 *
	 * @param a Attribute
	 * @param defaultValue Object
	 * @param m InputMethod
	 * @param additionalParams String[]
	 * @return ServiceBuilder
	 */
	public ServiceBuilder addStaticInput(Attribute a, Object defaultValue, InputMethod m, String... additionalParams) {
		Assert.assertNotNull(a);
		Assert.assertNotNull(m);
		this.attributes.add(a);
		this.staticInputs.put(a, m);
		this.staticvalues.put(a, defaultValue);
		if (additionalParams != null && additionalParams.length > 0) {
			for (String param : additionalParams) {
				this.inputParams.put(a, param);
			}
		}
		return this;
	}

	/**
	 *
	 * @param a Attribute
	 * @param defaultValue Object
	 * @return ServiceBuilder
	 */
	public ServiceBuilder addStaticInput(Attribute a, Object defaultValue) {
		this.addStaticInput(a, defaultValue, new InputMethod(a.getName()));
		return this;
	}

	/**
	 *
	 * @param a Attribute
	 * @param m InputMethod
	 * @param additionalParams String[]
	 * @return ServiceBuilder
	 */
	public ServiceBuilder addStaticInput(Attribute a, InputMethod m, String... additionalParams) {
		return this.addStaticInput(a, null, m, additionalParams) ;
	}

	/**
	 *
	 * @param a Attribute
	 * @param om OutputMethod
	 * @param im InputMethod
	 * @param additionalParams String[]
	 * @return ServiceBuilder
	 */
	public ServiceBuilder addAttribute(Attribute a, OutputMethod om, InputMethod im, String... additionalParams) {
		Assert.assertNotNull(a);
		this.attributes.add(a);
		this.nonStaticAttributes.add(a);
		this.outputMethods.put(a, om);
		if (im != null) {
			this.inputMethods.put(a, im);
		}
		if (additionalParams != null && additionalParams.length > 0) {
			for (String param : additionalParams) {
				this.inputParams.put(a, param);
			}
		}
		return this;
	}

	/**
	 *
	 * @param a Attribute
	 * @param om OutputMethod
	 * @return ServiceBuilder
	 */
	public ServiceBuilder addAttribute(Attribute a, OutputMethod om) {
		return this.addAttribute(a, om, new InputMethod(a.getName()));
	}
	
	/**
	 *
	 * @param primaryKey the key
	 * @return the service builder
	 */
	public ServiceBuilder addPrimaryKey(PrimaryKey primaryKey) {
		this.primaryKey = primaryKey;
		return this;
	}
	
	
	/**
	 *
	 * @param b AccessMethod
	 * @param c Cost
	 * @return ServiceBuilder
	 */
	public ServiceBuilder addAccessMethod(AccessMethodDescriptor b) {
		this.accessMethods.add(b);
		return this;
	}
	
	/**
	 *
	 * @return the attributes
	 */
	public List<Attribute> getAttributes() {
		return this.attributes;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.builder.Builder#build()
	 */
	@Override
	public Service build() {
		Assert.assertNotNull(this.protocol);
		switch (this.protocol.toLowerCase()) {
		case "rest":
			List<RESTAttribute> allAttributes = new ArrayList<>();
			List<RESTAttribute> nonStaticInputs = new ArrayList<>();
			for (Attribute a: this.attributes) {
				RESTAttribute r = null;
				if (this.staticInputs.keySet().contains(a)) {
					r = new RESTStaticInput<>(
							a,
							this.staticInputs.get(a),
							this.inputParams.get(a),
							this.staticvalues.get(a));
				} else {
					r = new RESTAttribute(
							a,
							this.outputMethods.get(a), 
							this.inputMethods.get(a),
							this.inputParams.get(a));
					nonStaticInputs.add(r);
				}
				allAttributes.add(r);
			}
			Relation result = new RESTRelation(this.name, 
					nonStaticInputs.toArray(new RESTAttribute[nonStaticInputs.size()]),
					this.accessMethods.toArray(new AccessMethodDescriptor[this.accessMethods.size()]),
					allAttributes.toArray(new RESTAttribute[allAttributes.size()]),
					this.url, this.mediaType, this.resultDelimiter, this.policies);
			result.setPrimaryKey(this.primaryKey);
			return (Service) result;
		default:
			throw new UnsupportedOperationException(this.protocol + " is not a supported protocol.");
		}
	}
}
