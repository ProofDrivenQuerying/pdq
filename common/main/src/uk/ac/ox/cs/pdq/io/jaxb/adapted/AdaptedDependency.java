package uk.ac.ox.cs.pdq.io.jaxb.adapted;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Formula;

/**
 * @author Gabor
 *
 */
public class AdaptedDependency {
	private Atom[] body;
	private Atom[] head;

	public AdaptedDependency() {
	}

	public AdaptedDependency(Formula body, Formula head) {
		this.setBody(body.getAtoms());
		this.setHead(head.getAtoms());
	}

	public Dependency toDependency() {
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

}
