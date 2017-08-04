package uk.ac.ox.cs.pdq.db;

import java.io.Serializable;
import java.lang.reflect.Type;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.io.jaxb.adapters.AttributeAdapter;
import uk.ac.ox.cs.pdq.util.Typed;

/**
 * Represents a relation's attribute.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
@XmlJavaTypeAdapter(AttributeAdapter.class)
public class Attribute implements Typed, Serializable {
	private static final long serialVersionUID = -2103116468417078713L;

	/**  The attribute's name. */
	protected final String name;

	/**  The attribute's type. */
	protected final Type type;

	/**  String representation of the object. */
	protected String toString = null;

	protected Attribute(Type type, String name) {
		Assert.assertNotNull(type);
		Assert.assertNotNull(name);
		assert name != null;
		this.type = type;
		this.name = name;
	}

	public Attribute(Attribute attribute) {
		this(attribute.type, attribute.name);
	}

	@Override
	public Type getType() {
		return this.type;
	}
	
	public String getName() {
		return this.name;
	}
	
	public static Attribute create(Type type, String name) {
		return Cache.attribute.intern(new Attribute(type, name));
	}

	@Override
	public String toString() {
		if (this.toString == null) {
			StringBuilder result = new StringBuilder();
			result.append(this.name);
			this.toString = result.toString().intern();
		}
		return this.toString;
	}
}
