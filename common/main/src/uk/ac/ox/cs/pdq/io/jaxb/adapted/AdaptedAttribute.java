package uk.ac.ox.cs.pdq.io.jaxb.adapted;

import java.io.Serializable;
import java.lang.reflect.Type;

import javax.xml.bind.annotation.XmlAttribute;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.util.Typed;

/**
 * @author Gabor
 *
 */
public class AdaptedAttribute implements Typed, Serializable {
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

	@Override
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
