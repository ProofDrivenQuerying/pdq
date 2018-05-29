package uk.ac.ox.cs.pdq.datasources.io.jaxb.servicegroup;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Mark Ridler
 *
 */
// ServiceGroupsRoot is the XML element which corresponds to the <service-groups> tag
// It is the root XML element for the service-groups.xml file
// It represents the in-memory results of parsing the XML file for the ServiceGroupsRoot object
// It contains  Attribute~Encodings, GroupUsagePolicies and Services.
@XmlRootElement (name="service-groups")
@XmlType (propOrder= {"attributeEncoding", "usagePolicy", "service"})
public class ServiceGroupsRoot {
	
	private AttributeEncoding[] attributeEncoding;
	private GroupUsagePolicy[] usagePolicy;
	private Service[] service;

	@XmlElement (name = "attribute-encoding", required=false)
	public AttributeEncoding[] getAttributeEncoding() {
		return attributeEncoding;
	}

	public void setAttributeEncoding(AttributeEncoding[] attributeEncoding) {
		this.attributeEncoding = attributeEncoding;
	}

	@XmlElement (name = "usage-policy", required=false)
	public GroupUsagePolicy[] getUsagePolicy() {
		return usagePolicy;
	}

	public void setUsagePolicy(GroupUsagePolicy[] usagePolicy) {
		this.usagePolicy = usagePolicy;
	}

	@XmlElement (required=true)
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
