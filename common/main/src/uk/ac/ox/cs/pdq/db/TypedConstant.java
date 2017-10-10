package uk.ac.ox.cs.pdq.db;

import java.io.Serializable;
import java.lang.reflect.Type;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.util.Typed;

/**
 * Schema constant.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 * @author Gabor
 */
public class TypedConstant extends Constant implements Typed, Serializable, Comparable<Constant> {
	private static final long serialVersionUID = 314066835619901611L;

	/**
	 * The constant's type. - Even though the type can be generated from the value
	 * we still need this in order to be able to deal with "typed nulls"
	 */
	private final Type type;

	/** The constant's value. */
	public final Object value;

	protected TypedConstant(Object value) {
		Assert.assertNotNull(value);
		this.type = value.getClass();
		this.value = value;
	}

	@Override
	public Type getType() {
		return this.type;
	}

	public Object getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return this.value.toString();
	}

	@Override
	public boolean isVariable() {
		return false;
	}

	@Override
	public boolean isUntypedConstant() {
		return false;
	}

	@Override
	public TypedConstant clone() {
		return new TypedConstant(this.value);
	}

	public static TypedConstant create(Object value) {
		return Cache.typedConstant.retrieve(new TypedConstant(value));
	}

	@Override
	public int compareTo(Constant con) {
		if (con instanceof UntypedConstant) {
			return -1;
		}
		TypedConstant o = (TypedConstant) con;
		if (this.type != o.type) {
			// numbers first, string after
			if (this.type.equals(Integer.class)) {
				return -1;
			}
			return 1;
		}
		if (this.type.equals(Integer.class)) {
			return ((Integer) this.getValue()).compareTo((Integer) o.getValue());
		}
		return ((String) this.getValue()).compareTo((String) o.getValue());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TypedConstant))
			return false;
		TypedConstant o = (TypedConstant) obj;
		if (this.type != o.type)
			return false;
		if (this.value == null && o.value == null)
			return true;
		if (this.value == null || o.value == null)
			return false;
		if (this.value instanceof String)
			return ((String) this.value).equals(o.value);
		if (this.value instanceof Integer)
			return ((Integer) this.value).equals(o.value);
		else
			return this.value.equals(o.value);
	}
}
