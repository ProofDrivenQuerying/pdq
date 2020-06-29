// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.jaxb.adapted;

import java.io.Serializable;
import java.lang.reflect.Type;

import javax.xml.bind.annotation.XmlAttribute;

import uk.ac.ox.cs.pdq.db.Attribute;

/**
 * @author Gabor
 *
 */
public class AdaptedAttribute implements Serializable {
	private static final long serialVersionUID = -2103116468417078713L;

	/** The attribute's name. */
	protected String name;

	/** The attribute's type. */
	protected Type type;

	public AdaptedAttribute() {
	}

	public AdaptedAttribute(Attribute v) {
		name = v.getName();
		type = v.getType();
	}

	public Type getType() {
		return this.type;
	}

	@XmlAttribute(name = "type")
	public String getTypeName() {
		return this.type.getTypeName();
	}

	public void setTypeName(String type) throws ClassNotFoundException {
		this.type = Class.forName(type);
	}

	@XmlAttribute
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Attribute toAttribute() {
		return Attribute.create(getType(), getName());
	}
}
