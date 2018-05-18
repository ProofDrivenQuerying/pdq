package uk.ac.ox.cs.pdq.datasources.io.jaxb.servicegroup;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Mark Ridler
 *
 */
// AccessMethodRoot is the XML element which corresponds to the <access-method> tag
@XmlType (propOrder= {"name", "type", "value", "template"})
public class AttributeEncoding {
	
	private String name;
	private String type;
	private String value;
	private String template;
	
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


	@XmlAttribute
	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}


	@XmlAttribute
	public String getTemplate() {
		return template;
	}


	public void setTemplate(String template) {
		this.template = template;
	}


	public String toString()
	{
		return "AttributeEncoding";
	}

}
