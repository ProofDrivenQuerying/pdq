package uk.ac.ox.cs.pdq.io.jaxb.adapted;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.TGD;

/**
 * @author Gabor
 *
 */
public class AdaptedDependency {
	private Atom[] body;
	private Atom[] head;
	private String type = "TGD";
	
	public AdaptedDependency() {
	}
	
	public AdaptedDependency(Dependency d) {
		this.setBody(d.getBody().getAtoms());
		this.setHead(d.getHead().getAtoms());
		if (d instanceof EGD)
			type="EGD";
		else if (d instanceof TGD)
			type="TGD";
		else 
			type = d.getClass().getName();
	}

	public Dependency toDependency() {
		if ("TGD".equals(type))
			return TGD.create(body, head);
		if ("EGD".equals(type)) {
			return EGD.create(body, head);
		}
		return Dependency.create(body, head);
	}

	@XmlElement(name = "atom")
	@XmlElementWrapper(name = "body")
	public Atom[] getBody() {
		return body;
	}

	@XmlElement(name = "atom")
	@XmlElementWrapper(name = "head")
	public Atom[] getHead() {
		return this.head;
	}

	public void setHead(Atom[] head) {
		this.head = head;
	}

	public void setBody(Atom[] body) {
		this.body = body;
	}

	@XmlAttribute(name = "type")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
