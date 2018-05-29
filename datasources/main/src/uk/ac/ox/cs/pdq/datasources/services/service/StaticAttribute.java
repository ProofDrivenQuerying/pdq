package uk.ac.ox.cs.pdq.datasources.services.service;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Mark Ridler
 *
 */
// StaticAttribute is the XML element which corresponds to the <static-attribute> tag
// It represents the in-memory results of parsing the XML file for the StaticAttribute object.
// It has attributes for name, type, attribute-encoding, attribute-encoding-index and value
@XmlType (propOrder= {"name", "type", "attributeEncoding", "attributeEncodingIndex", "value"})
public class StaticAttribute {
	
	private String name;
	private String type;
	private String attributeEncoding;
	private String attributeEncodingIndex;
	private String value;
	
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


	@XmlAttribute (name = "attribute-encoding", required=false)
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

	@XmlAttribute (required=false)
	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}


	public String toString()
	{
		return "StaticAttribute";
	}
	
}
