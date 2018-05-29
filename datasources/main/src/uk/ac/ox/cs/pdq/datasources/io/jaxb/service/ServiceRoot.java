package uk.ac.ox.cs.pdq.datasources.io.jaxb.service;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Mark Ridler
 *
 */
// ServiceRoot is the XML element which corresponds to the <service> tag.
// It is the root level object for the $service.xml file.
// It represents the in-memory results of parsing the XML file for the Service object.
// It contains policy, static-attribute and access-method elements
// It also has attributes for url, documentation, media-type and ressult-delimiter
@XmlRootElement (name="service")
@XmlType (propOrder= {"url", "documentation", "mediaType", "resultDelimiter", "serviceUsagePolicy", "staticAttribute", "accessMethod"})
public class ServiceRoot {
	
	private String url;
	private String documentation;
	private String mediaType;
	private String resultDelimiter;
	private ServiceUsagePolicy[] serviceUsagePolicy;
	private StaticAttribute[] staticAttribute;
	private AccessMethod[] accessMethod;
	
	@XmlAttribute (required=true)
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@XmlAttribute (required=false)
	public String getDocumentation() {
		return documentation;
	}

	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}

	@XmlAttribute (name = "media-type", required=false)
	public String getMediaType() {
		return mediaType;
	}

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}
	
	@XmlAttribute (name = "result-delimiter", required=false)
	public String getResultDelimiter() {
		return resultDelimiter;
	}

	public void setResultDelimiter(String resultDelimiter) {
		this.resultDelimiter = resultDelimiter;
	}

	@XmlElement (name = "policy", required=false)
	public ServiceUsagePolicy[] getServiceUsagePolicy() {
		return serviceUsagePolicy;
	}

	public void setServiceUsagePolicy(ServiceUsagePolicy[] serviceUsagePolicy) {
		this.serviceUsagePolicy = serviceUsagePolicy;
	}

	@XmlElement (name = "static-attribute", required=false)
	public StaticAttribute[] getStaticAttribute() {
		return staticAttribute;
	}

	public void setStaticAttribute(StaticAttribute[] staticAttribute) {
		this.staticAttribute = staticAttribute;
	}

	@XmlElement (name = "access-method", required=true)
	public AccessMethod[] getAccessMethod() {
		return accessMethod;
	}

	public void setAccessMethod(AccessMethod[] accessMethod) {
		this.accessMethod = accessMethod;
	}

	public String toString()
	{
		return "Service";
	}
}
