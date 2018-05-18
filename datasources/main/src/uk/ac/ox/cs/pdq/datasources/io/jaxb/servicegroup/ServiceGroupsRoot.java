package uk.ac.ox.cs.pdq.datasources.io.jaxb.servicegroup;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.ox.cs.pdq.datasources.services.AttributeEncoding2;

/**
 * @author Mark Ridler
 *
 */
// AccessMethodRoot is the XML element which corresponds to the <access-method> tag
@XmlRootElement (name="service-groups")
@XmlType (propOrder= {"attributeEncoding", "usagePolicy", "service"})
public class ServiceGroupsRoot {
	
	private AttributeEncoding[] attributeEncoding;
	private UsagePolicy[] usagePolicy;
	private Service[] service;

	@XmlElement (name = "attribute-encoding")
	public AttributeEncoding[] getAttributeEncoding() {
		return attributeEncoding;
	}

	public void setAttributeEncoding(AttributeEncoding[] attributeEncoding) {
		this.attributeEncoding = attributeEncoding;
	}

	@XmlElement (name = "usage-policy")
	public UsagePolicy[] getUsagePolicy() {
		return usagePolicy;
	}

	public void setUsagePolicy(UsagePolicy[] usagePolicy) {
		this.usagePolicy = usagePolicy;
	}

	@XmlElement
	public Service[] getService() {
		return service;
	}

	public void setService(Service[] service) {
		this.service = service;
	}

	public String toString()
	{
		return "Services";
	}

}
