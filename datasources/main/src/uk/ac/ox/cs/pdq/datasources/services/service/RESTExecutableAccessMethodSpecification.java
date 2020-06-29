// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

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
@XmlType (propOrder= {"name", "type", "cost", "relationName", "attributes"})
public class RESTExecutableAccessMethodSpecification {
	
	private String name;
	private String type;
	private String cost;
	private String relationName;
	private RESTExecutableAccessMethodAttributeSpecification[] attributes;
	
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

	@XmlAttribute (name="relation-name", required=true)
	public String getRelationName() {
		return relationName;
	}

	public void setRelationName(String relationName) {
		this.relationName = relationName;
	}

	@XmlElement (name="attribute", required=true)
	public RESTExecutableAccessMethodAttributeSpecification[] getAttributes() {
		return attributes;
	}

	public void setAttributes(RESTExecutableAccessMethodAttributeSpecification[] attributes) {
		this.attributes = attributes;
	}

	public String toString()
	{
		return "AccessMethod";
	}
}
