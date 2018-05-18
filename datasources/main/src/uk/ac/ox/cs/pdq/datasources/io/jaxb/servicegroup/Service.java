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
@XmlType (propOrder= {"name"})
public class Service {
	
	private String name;
	
	@XmlAttribute
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
