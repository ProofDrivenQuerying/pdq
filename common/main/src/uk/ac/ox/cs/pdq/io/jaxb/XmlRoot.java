package uk.ac.ox.cs.pdq.io.jaxb;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;

/**
 *  Root object for exporting RelationalTerm object to xml. 
 * @author Gabor
 *
 */
@XmlRootElement
public class XmlRoot {
	private RelationalTerm t;
	public XmlRoot() {
	}
	@XmlElement
	public RelationalTerm getT() {
		return t;
	}
	
	@XmlElement
	public String getName() {
		return t.toString();
	}
	
	public void setT(RelationalTerm t) {
		this.t = t;
	}
}
