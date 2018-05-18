package uk.ac.ox.cs.pdq.datasources.io.jaxb.service;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Mark Ridler
 *
 */
// AccessMethodRest is the XML element which corresponds to the <rest> tag
@XmlType (propOrder= {"name", "attributeEncoding", "value"})
public class StaticAttribute {
	
	private String name;
	private String attributeEncoding;
	private String value;
	
	@XmlAttribute
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute (name = "attribute-encoding")
	public String getAttributeEncoding() {
		return attributeEncoding;
	}

	public void setAttributeEncoding(String attributeEncoding) {
		this.attributeEncoding = attributeEncoding;
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
