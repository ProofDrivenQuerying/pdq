// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.services.service;

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
// It contains static-attribute and access-method elements
// It also has attributes for url, documentation, media-type and ressult-delimiter
@XmlRootElement (name="service")
@XmlType (propOrder= {"name", "url", "documentation", "mediaType", "resultDelimiter", "serviceGroup", "staticAttribute", "accessMethod"})
public class Service {
	
	private String name;
	private String url;
	private String documentation;
	private String mediaType;
	private String resultDelimiter;
	private String serviceGroup;
	private StaticAttribute[] staticAttribute;
	private RESTExecutableAccessMethodSpecification[] accessMethod;
	

	@XmlAttribute (name = "name", required=true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

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

	@XmlAttribute (name = "service-group", required=true)
	public String getServiceGroup() {
		return serviceGroup;
	}

	public void setServiceGroup(String serviceGroup) {
		this.serviceGroup = serviceGroup;
	}

	@XmlElement (name = "static-attribute", required=false)
	public StaticAttribute[] getStaticAttribute() {
		return staticAttribute;
	}

	public void setStaticAttribute(StaticAttribute[] staticAttribute) {
		this.staticAttribute = staticAttribute;
	}

	@XmlElement (name = "access-method", required=true)
	public RESTExecutableAccessMethodSpecification[] getAccessMethod() {
		return accessMethod;
	}

	public void setAccessMethod(RESTExecutableAccessMethodSpecification[] accessMethod) {
		this.accessMethod = accessMethod;
	}

	public String toString()
	{
		return "Service";
	}
}
