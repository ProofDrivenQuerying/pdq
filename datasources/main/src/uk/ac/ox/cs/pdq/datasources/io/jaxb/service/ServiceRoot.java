package uk.ac.ox.cs.pdq.datasources.io.jaxb.service;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Mark Ridler
 *
 */
// ServiceRoot is the XML element which corresponds to the <service> tag
@XmlRootElement (name="service")
@XmlType (propOrder= {"url", "documentation", "mediaType", "staticAttribute", "accessMethod"})
public class ServiceRoot {
	
	private String url;
	private String documentation;
	private String mediaType;
	private StaticAttribute[] staticAttribute;
	private AccessMethod[] accessMethod;
	
	@XmlAttribute
	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}


	@XmlAttribute
	public String getDocumentation() {
		return documentation;
	}


	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}


	@XmlAttribute (name = "media-type")
	public String getMediaType() {
		return mediaType;
	}


	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}


	@XmlElement (name = "static-attribute")
	public StaticAttribute[] getStaticAttribute() {
		return staticAttribute;
	}

	public void setStaticAttribute(StaticAttribute[] staticAttribute) {
		this.staticAttribute = staticAttribute;
	}

	@XmlElement (name = "access-method")
	public AccessMethod[] getAccessMethod() {
		return accessMethod;
	}

	public void setAccessMethod(AccessMethod[] accessMethod) {
		this.accessMethod = accessMethod;
	}

	public String toString()
	{
		return "Services";
	}

}
