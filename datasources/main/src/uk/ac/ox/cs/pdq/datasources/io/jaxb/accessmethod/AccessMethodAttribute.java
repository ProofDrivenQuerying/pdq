package uk.ac.ox.cs.pdq.datasources.io.jaxb.accessmethod;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Mark Ridler
 *
 */
// AccessMethodAttribute is the XML element corresponding to the <attribute> tag
@XmlType (propOrder= {"name", "type", "value", "input", "output", "encoding", "relationAttribute"})
public class AccessMethodAttribute {
	
	private String name;
	private String type;
	private String input;
	private String output;
	private String value;
	private String encoding;
	private String relationAttribute;
	
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
	public String getInput() {
		return input;
	}
	public void setInput(String input) {
		this.input = input;
	}
	@XmlAttribute
	public String getOutput() {
		return output;
	}
	public void setOutput(String output) {
		this.output = output;
	}
	@XmlAttribute
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	@XmlAttribute
	public String getEncoding() {
		return encoding;
	}
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	@XmlAttribute (name = "relation-attribute")
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
