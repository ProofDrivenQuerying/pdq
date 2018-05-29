package uk.ac.ox.cs.pdq.datasources.services.servicegroup;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Mark Ridler
 *
 */
// AttributeEncoding is the XML element which corresponds to the <attribute-encoding> tag
// It represents the in-memory results of parsing the XML file for the AttributeEncoding object
// It has attributes for name, type, value and template
//   name = the name of this AttributeEncoding to be referred to elsewhere
//   type = path-element or url-param depending on the encoding method
//   value = the default static value for this encoding
//   template = for example /{1}/{2} the template specification for path-element
@XmlType (propOrder= {"name", "type", "value", "template"})
public class AttributeEncoding implements Comparable<AttributeEncoding> {
	
	private String name;
	private String type;
	private String value;
	private String template;
	
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
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}


	@XmlAttribute (required=false)
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


	@Override
	public int compareTo(AttributeEncoding o) {
		return (name + type + value + template).compareTo(o.name + o.type + o.value + o.template);
	}

}
