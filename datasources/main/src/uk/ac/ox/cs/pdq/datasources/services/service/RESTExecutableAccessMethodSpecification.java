package uk.ac.ox.cs.pdq.datasources.services.service;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Mark Ridler
 *
 */
// AccessMethod is the XML element which corresponds to the <access-method> tag.
// It represents the results of parsing the XML as an in-memory object.
// It contains AccessMethodAttributes and has name, type and cost attributes.
@XmlType (propOrder= {"name", "type", "cost", "attributes"})
public class RESTExecutableAccessMethodSpecification {
	
	private String name;
	private String type;
	private String cost;
	private RESTExecutableAccessMethodAttributeSoecification[] attributes;
	
	@XmlAttribute (required=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute (required=true)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@XmlAttribute (required=false)
	public String getCost() {
		return cost;
	}

	public void setCost(String cost) {
		this.cost = cost;
	}

	@XmlElement (name="attribute", required=true)
	public RESTExecutableAccessMethodAttributeSoecification[] getAttributes() {
		return attributes;
	}

	public void setAttributes(RESTExecutableAccessMethodAttributeSoecification[] attributes) {
		this.attributes = attributes;
	}

	public String toString()
	{
		return "AccessMethod";
	}
}
