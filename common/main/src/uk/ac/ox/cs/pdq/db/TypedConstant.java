package uk.ac.ox.cs.pdq.db;

import java.io.Serializable;
import java.lang.reflect.Type;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.util.Typed;

/**
 * Schema constant.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class TypedConstant extends Constant implements Typed, Serializable {
	private static final long serialVersionUID = 314066835619901611L;

	//TODO remove the type
	/**  The constant's type. */
	private final Type type;

	/**  The constant's value. */
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
        return Cache.typedConstant.intern(new TypedConstant(value));
    }
	
}
