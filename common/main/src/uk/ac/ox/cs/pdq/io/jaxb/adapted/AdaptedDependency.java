// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.jaxb.adapted;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.TGD;

/**
 * @author Gabor
 *
 */
@XmlType(propOrder = { "name", "body", "head" })
public class AdaptedDependency {
	private Atom[] body;
	private Atom[] head;
	private String type = "TGD";
	private String name = "dependency";
	
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
			return TGD.create(body, head, name);
		if ("EGD".equals(type)) {
			return EGD.create(body, head, name);
		}
		return Dependency.create(body, head);
	}

	@XmlAttribute(name = "name")
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
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
