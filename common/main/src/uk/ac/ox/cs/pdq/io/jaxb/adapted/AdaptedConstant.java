package uk.ac.ox.cs.pdq.io.jaxb.adapted;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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
		} else if (type!=null && type == java.sql.Date.class) {
			SimpleDateFormat sdfmt1 = new SimpleDateFormat("yyyy-MM-dd");
			java.util.Date dDate;
			try {
				dDate = sdfmt1.parse( value );
				ret = TypedConstant.create(new java.sql.Date(dDate.getTime()));
			} catch (ParseException e) {
				// ignored, could be user's incorrect xml file.
				e.printStackTrace();
				ret = TypedConstant.create(value);
			}
		} else {
			Constructor<?> constructor=null;
			try {
				if (type != null) constructor = Class.forName(type.getTypeName()).getConstructor(String.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (constructor!=null) {
				try {
					ret = TypedConstant.create(constructor.newInstance(value));
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			if (ret == null)
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
		this.type = TypedConstant.convertStringToType(type);
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
