package uk.ac.ox.cs.pdq.datasources.io.jaxb.service;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Mark Ridler
 *
 */
// StaticAttribute is the XML element which corresponds to the <static-attribute> tag
@XmlType (propOrder= {"name", "type", "attributeEncoding", "attributeEncodingIndex", "value"})
public class StaticAttribute {
	
	private String name;
	private String type;
	private String attributeEncoding;
	private String attributeEncodingIndex;
	private String value;
	
	@XmlAttribute
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}

	
	@XmlAttribute
	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	@XmlAttribute (name = "attribute-encoding")
	public String getAttributeEncoding() {
		return attributeEncoding;
	}

	public void setAttributeEncoding(String attributeEncoding) {
		this.attributeEncoding = attributeEncoding;
	}

	@XmlAttribute (name = "attribute-encoding-index")
	public String getAttributeEncodingIndex() {
		return attributeEncodingIndex;
	}

	public void setAttributeEncodingIndex(String attributeEncodingIndex) {
		this.attributeEncodingIndex = attributeEncodingIndex;
	}

	@XmlAttribute
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
