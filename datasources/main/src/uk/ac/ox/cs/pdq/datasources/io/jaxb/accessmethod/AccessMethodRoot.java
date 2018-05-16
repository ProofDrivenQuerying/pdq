package uk.ac.ox.cs.pdq.datasources.io.jaxb.accessmethod;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.ox.cs.pdq.datasources.services.RESTExecutableAccessMethod;
import uk.ac.ox.cs.pdq.datasources.utility.Table;
import uk.ac.ox.cs.pdq.util.Tuple;

/**
 * @author Mark Ridler
 *
 */
// AccessMethodRoot is the XML element which corresponds to the <access-method> tag
@XmlRootElement (name="access-method")
@XmlType (propOrder= {"name", "type", "cost", "rest", "attributes"})
public class AccessMethodRoot {
	
	private String name;
	private String type;
	private String cost;
	private AccessMethodRest rest;
	private AccessMethodAttribute[] attributes;
	
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
	public String getCost() {
		return cost;
	}

	public void setCost(String cost) {
		this.cost = cost;
	}

	@XmlElement (name="rest")
	public AccessMethodRest getRest() {
		return rest;
	}

	public void setRest(AccessMethodRest rest) {
		this.rest = rest;
	}

	@XmlElement (name="attribute")
	public AccessMethodAttribute[] getAttributes() {
		return attributes;
	}

	public void setAttributes(AccessMethodAttribute[] attributes) {
		this.attributes = attributes;
	}

	public String toString()
	{
		return "AccessMethod";
	}
	
	// Performs a service access for an access method with Table as input and result
	public Table restAccess(Tuple input)
	{
		RESTExecutableAccessMethod ream = new
			RESTExecutableAccessMethod(rest.getUrl(), rest.getDocumentation(), rest.getMediaType(), this);
		return ream.access(input);
	}

}
