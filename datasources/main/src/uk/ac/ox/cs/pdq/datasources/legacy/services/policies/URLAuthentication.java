package uk.ac.ox.cs.pdq.datasources.legacy.services.policies;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import uk.ac.ox.cs.pdq.datasources.services.AccessPreProcessor;
import uk.ac.ox.cs.pdq.datasources.services.ServiceRepository;
import uk.ac.ox.cs.pdq.datasources.services.rest.InputMethod;
import uk.ac.ox.cs.pdq.datasources.services.rest.RESTAccess;
import uk.ac.ox.cs.pdq.datasources.services.rest.RESTAttribute;
import uk.ac.ox.cs.pdq.datasources.services.rest.RESTRequestEvent;
import uk.ac.ox.cs.pdq.db.Attribute;

import com.google.common.base.Preconditions;

/**
 * Authentication policy than passes credential as URL parameters.
 * 
 * @author Julien Leblay
 *
 */
public class URLAuthentication  implements UsagePolicy, AccessPreProcessor<RESTRequestEvent> {

	protected static final String INPUT_METHOD = "input-method";

	/** The request attribute where the credential is defined. */
	protected RESTAttribute keyAttributes;

	/**  The set of input methods used by this policy. */
	private Set<InputMethod> inputMethods = new LinkedHashSet<>();		
	
	/**
	 * Default constructor.
	 *
	 * @param keyAtt the key att
	 */
	protected URLAuthentication(RESTAttribute keyAtt) {
		super();
		Preconditions.checkArgument(keyAtt != null);
		Preconditions.checkArgument(keyAtt.getInputMethod() != null);
		this.keyAttributes = keyAtt;
		this.inputMethods.add(this.keyAttributes.getInputMethod());
	}

	/**
	 * Constructor used by the usage policy factory.
	 *
	 * @param repo the repo
	 * @param properties the properties
	 */
	public URLAuthentication(ServiceRepository repo, Properties properties) {
		this(new RESTAttribute(
				Attribute.create(String.class, repo.getInputMethod(properties.getProperty(INPUT_METHOD)).getName()),
			repo.getInputMethod(properties.getProperty(INPUT_METHOD))));
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.wrappers.service.UsagePolicy#copy()
	 */
	@Override
	public UsagePolicy copy() {
		return new URLAuthentication(this.keyAttributes);
	}

	/**
	 *
	 * @param event RESTRequestEvent
	 * @throws UsagePolicyViolationException the usage policy violation exception
	 */
	@Override
	public void processAccessRequest(RESTRequestEvent event) throws UsagePolicyViolationException {
		RESTAccess access = event.getAccess();
		Map<String, Object> inputParams = new LinkedHashMap<>();
		inputParams.put(
				this.keyAttributes.getInputMethod().getName(),
				this.keyAttributes.getInputMethod().getDefaultValue());
		access.processParams(this.inputMethods, inputParams);
	}
}
