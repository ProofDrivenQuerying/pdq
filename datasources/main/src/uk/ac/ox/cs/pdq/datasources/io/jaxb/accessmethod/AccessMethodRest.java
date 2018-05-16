package uk.ac.ox.cs.pdq.datasources.io.jaxb.accessmethod;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.ox.cs.pdq.datasources.utility.Table;

/**
 * @author Mark Ridler
 *
 */
// AccessMethodRest is the XML element which corresponds to the <rest> tag
@XmlType (propOrder= {"url", "documentation", "mediaType"})
public class AccessMethodRest {
	
	private String url;
	private String documentation;
	private String mediaType;
	
	@XmlAttribute
	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}


	@XmlAttribute
	public String getDocumentation() {
		return documentation;
	}


	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}


	@XmlAttribute (name = "media-type")
	public String getMediaType() {
		return mediaType;
	}


	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}


	public String toString()
	{
		return "Rest";
	}
	
}
