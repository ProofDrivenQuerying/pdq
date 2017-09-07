package uk.ac.ox.cs.pdq.io.jaxb.adapted;

import java.lang.reflect.Type;

import javax.xml.bind.annotation.XmlAttribute;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;

/**
 * @author Gabor
 *
 */
public class AdaptedConstant extends AdaptedVariable {
	private Type type;
	private String value;

	public AdaptedConstant() {
	}

	public AdaptedConstant(Constant value) {
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
		return toConstant(null);
	}
	public Constant toConstant(Type preferedType) {
		if (type == null) {
			type = preferedType;
		}
		TypedConstant ret = null;
		if (type != null && type == Double.class) {
			ret = TypedConstant.create(new Double(Double.parseDouble(value)));
		} else if (type != null && type == Integer.class) {
			try {
				ret = TypedConstant.create(new Integer(Integer.parseInt(value)));
			}catch (NumberFormatException e) {
				ret = TypedConstant.create(value);
			}
		} else {
			ret = TypedConstant.create(value);
		}
		if (type!=null && ret.getType() != type) {
			throw new IllegalArgumentException("Type should match!");
		}
		return ret;
	}

	public void setType(Type type) {
		this.type = type;
	}
	@XmlAttribute(name = "type")
	public void setType(String type) {
		if (type!=null && type.equalsIgnoreCase("String.class"))
			this.type = String.class;
		if (type!=null && type.equalsIgnoreCase("Integer.class"))
			this.type = Integer.class;
		if (type!=null && type.equalsIgnoreCase("Double.class"))
			this.type = Double.class;
	}
	public String getType() {
		if (type!= null && type != String.class)
			return type.getTypeName();
		return null;
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
