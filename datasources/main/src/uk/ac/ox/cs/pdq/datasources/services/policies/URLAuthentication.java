// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.services.policies;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.datasources.legacy.services.AccessPreProcessor;
import uk.ac.ox.cs.pdq.datasources.legacy.services.ServiceRepository;
import uk.ac.ox.cs.pdq.datasources.legacy.services.rest.InputMethod;
import uk.ac.ox.cs.pdq.datasources.legacy.services.rest.RESTAccess;
import uk.ac.ox.cs.pdq.datasources.legacy.services.rest.RESTAttribute;
import uk.ac.ox.cs.pdq.datasources.legacy.services.rest.RESTRequestEvent;
import uk.ac.ox.cs.pdq.datasources.services.servicegroup.GroupUsagePolicy;
import uk.ac.ox.cs.pdq.db.Attribute;

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
	
	private String attributeEncoding;
	
	protected URLAuthentication(RESTAttribute keyAtt) {
		super();
		Preconditions.checkArgument(keyAtt != null);
		Preconditions.checkArgument(keyAtt.getInputMethod() != null);
		this.keyAttributes = keyAtt;
		this.inputMethods.add(this.keyAttributes.getInputMethod());
	}

	protected URLAuthentication(String attributeEncoding) {
		super();
		this.attributeEncoding = attributeEncoding;
	}

	public URLAuthentication(ServiceRepository repo, Properties properties) {
		this(new RESTAttribute(
				Attribute.create(String.class, repo.getInputMethod(properties.getProperty(INPUT_METHOD)).getName()),
			repo.getInputMethod(properties.getProperty(INPUT_METHOD))));
	}

	public URLAuthentication(GroupUsagePolicy gup) {
		this(gup.getAttributeEncoding());
	}

	@Override
	public UsagePolicy copy() {
		return new URLAuthentication(this.keyAttributes);
	}

	public String getAttributeEncoding() {
		return attributeEncoding;
	}

	public void setAttributeEncoding(String attributeEncoding) {
		this.attributeEncoding = attributeEncoding;
	}

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
