// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.services.service;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Mark Ridler
 *
 */
// AccessMethodAttribute is the XML element corresponding to the <attribute> tag.
// It represents the parsed results of the XML as an in-memory object.
// It has attributes for name, type, value, input, output, attribute-encoding, attribute-encoding-index and relation-attribute
@XmlType (propOrder= {"name", "type", "value", "input", "output", "attributeEncoding", "attributeEncodingIndex", "relationAttribute"})
public class RESTExecutableAccessMethodAttributeSpecification {
	
	private String name;
	private String type;
	private String value;
	private String input;
	private String output;
	private String attributeEncoding;
	private String attributeEncodingIndex;
	private String relationAttribute;
	
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
	public String getValue() {
		return (value == null) ? "" : value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	@XmlAttribute (required=false)
	public String getInput() {
		return input;
	}
	public void setInput(String input) {
		this.input = input;
	}
	@XmlAttribute (required=false)
	public String getOutput() {
		return output;
	}
	public void setOutput(String output) {
		this.output = output;
	}
	@XmlAttribute (name="attribute-encoding", required=false)
	public String getAttributeEncoding() {
		return attributeEncoding;
	}
	public void setAttributeEncoding(String attributeEncoding) {
		this.attributeEncoding = attributeEncoding;
	}
	@XmlAttribute (name = "attribute-encoding-index", required=false)
	public String getAttributeEncodingIndex() {
		return attributeEncodingIndex;
	}
	public void setAttributeEncodingIndex(String attributeEncodingIndex) {
		this.attributeEncodingIndex = attributeEncodingIndex;
	}
	@XmlAttribute (name = "relation-attribute", required=false)
	public String getRelationAttribute() {
		return relationAttribute;
	}
	public void setRelationAttribute(String relationAttribute) {
		this.relationAttribute = relationAttribute;
	}
	public String toString()
	{
		return "Attribute";
	}
	
}
