package uk.ac.ox.cs.pdq.datasources.io.jaxb.servicegroup;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Mark Ridler
 *
 */
// Service is the XML element which corresponds to the <service> tag
// This is an empty tag with name attribute only
@XmlType (propOrder= {"name"})
public class Service {
	
	private String name;
	
	@XmlAttribute (required=true)
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String toString()
	{
		return "Service";
	}

}
