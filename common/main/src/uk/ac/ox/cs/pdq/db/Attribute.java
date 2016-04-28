package uk.ac.ox.cs.pdq.db;

import java.lang.reflect.Type;
import java.util.Objects;

import uk.ac.ox.cs.pdq.util.Typed;

import com.google.common.base.Preconditions;

/**
 * An attribute.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class Attribute implements Typed {

	/** The prefix to use when generating attribute names. */
	public static final String GENERATED_ATTRIBUTE_PREFIX = "x";

	/**  The attribute's name. */
	protected final String name;

	/**  The attribute's type. */
	protected final Type type;

	/** Cached instance hash (only possible because variables are immutable). */
	private final int hash;

	/** Cached String representation of a variable. */
	private final String rep;

	/**
	 * Default constructor.
	 *
	 * @param type
	 * 		The attribute's type
	 * @param name
	 *      The attribute's name
	 */
	public Attribute(Type type, String name) {
		Preconditions.checkArgument(type != null);
		Preconditions.checkArgument(name != null);
		assert name != null;
		this.type = type;
		this.name = name;
		this.hash = Objects.hash(this.name, this.type);
		StringBuilder result = new StringBuilder();
		result.append(this.name);
		this.rep = result.toString().intern();
	}

	/**
	 * Constructor for Attribute.
	 * @param attribute Attribute
	 */
	public Attribute(Attribute attribute) {
		this(attribute.type, attribute.name);
	}

	/**
	 * Gets the type.
	 *
	 * @return Class<?>
	 * @see uk.ac.ox.cs.pdq.util.Typed#getType()
	 */
	@Override
	public Type getType() {
		return this.type;
	}

	/**
	 * Gets the name.
	 *
	 * @return String
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Equals.
	 *
	 * @param o Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return this.getClass().isInstance(o)
				&& this.name.equals(((Attribute) o).name)
				&& this.type.equals(((Attribute) o).type);

	}

	/**
	 * Hash code.
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
		return this.hash;
	}

	/**
	 * To string.
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		return this.rep;
	}
}
