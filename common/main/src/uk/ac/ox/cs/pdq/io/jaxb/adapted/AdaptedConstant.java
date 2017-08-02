package uk.ac.ox.cs.pdq.io.jaxb.adapted;

import java.lang.reflect.Type;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.io.jaxb.adapters.ConstantAdapter;

/**
 * @author Gabor
 *
 */
@XmlJavaTypeAdapter(ConstantAdapter.class)
public class AdaptedConstant extends AdaptedVariable {
	private Type type;
	private String value;

	public AdaptedConstant() {
	}

	public AdaptedConstant(Constant value) {
		Assert.assertNotNull(value);
		if (value instanceof TypedConstant) {
			this.type = ((TypedConstant) value).getType();
			this.value = String.valueOf(((TypedConstant) value).getValue());

		} else if (value instanceof UntypedConstant) {
			this.value = ((UntypedConstant) value).getSymbol();
		} else {
			throw new IllegalArgumentException();
		}
	}

	@XmlAttribute(name = "value")
	public String getValue() {
		return this.value;
	}

	public Constant toConstant() {
		if (type == null)
			return UntypedConstant.create(value);
		TypedConstant ret = null;
		if (type != null && type == Integer.class) {
			ret = TypedConstant.create(Integer.parseInt(value));
		} else {
			ret = TypedConstant.create(value);
		}
		if (ret.getType() != type) {
			throw new IllegalArgumentException("Type should match!");
		}
		return ret;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setValue(Integer value) {
		this.value = "" + value;
	}

	public String toString() {
		return value;
	}
}
