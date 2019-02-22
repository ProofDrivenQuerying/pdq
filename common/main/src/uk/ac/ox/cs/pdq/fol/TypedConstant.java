package uk.ac.ox.cs.pdq.fol;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Assert;

/**
 * Schema constant.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 * @author Gabor
 */
public class TypedConstant extends Constant implements Serializable, Comparable<Constant> {
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

	public Type getType() {
		return this.type;
	}

	public Object getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		if (getValue() instanceof Date) {
			SimpleDateFormat sdfmt1 = new SimpleDateFormat("yyyy-MM-dd");
			return sdfmt1.format(getValue());
		}
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

	/**
	 * When a TypedConstant was converted into a string by the serializeToString() method, thismathod can reverse it and create a TypedConstant object out of that String.
	 * 
	 * TypedConstants are serialised as: <br>
	 * <code> "_Typed" + ((TypedConstant)term).getType() + "_" + ((TypedConstant)term).getValue(); </code> 
	 * @param serializedTypedConstant
	 * @return
	 */
	public static TypedConstant deSerializeTypedConstant(String serializedTypedConstant) {
		if (serializedTypedConstant != null && serializedTypedConstant.startsWith("_Typed")) {
			String typeAndValue = serializedTypedConstant.substring(6);
			String type = typeAndValue.substring(6, typeAndValue.indexOf('_'));
			String value = typeAndValue.substring(typeAndValue.indexOf('_') + 1);
			return TypedConstant.create(convertStringToType(value, convertStringToType(type)));
		}
		return TypedConstant.create(serializedTypedConstant);
	}

	/**
	 * Converts a given value to a required type. For example the string "500" can
	 * be converted to an Integer, Double, etc typed java objects.
	 * 
	 * @param value
	 * @param type
	 * @return
	 */
	public static Object convertStringToType(String value, Type type) {
		if (type == null || value == null) {
			return value;
		}
		if (type == Double.class) {
			return (Double)(Double.parseDouble(value));
		} else if (type == Integer.class) {
			try {
				return (Integer)(Integer.parseInt(value));
			} catch (NumberFormatException e) {
				return value;
			}
		} else if (type == java.sql.Date.class) {
			SimpleDateFormat sdfmt1 = new SimpleDateFormat("yyyy-MM-dd");
			java.util.Date dDate;
			try {
				dDate = sdfmt1.parse(value);
				return new java.sql.Date(dDate.getTime());
			} catch (ParseException e) {
				return value;
			}
		} else if (type == java.util.Date.class) {
			SimpleDateFormat sdfmt1 = new SimpleDateFormat("yyyy-MM-dd");
			try {
				return sdfmt1.parse(value);
			} catch (ParseException e) {
				return value;
			}
		} else {
			try {
				Constructor<?> constructor = Class.forName(type.getTypeName()).getConstructor(String.class);
				if (constructor == null) {
					return value;
				}
				return constructor.newInstance(value);
			} catch (Exception e) {
				return value;
			}
		}
	}

	/**
	 * Converts a String to a type object. For example the "Integer.class" or the
	 * "java.lang.Integer" strings will be returned as the Integer java type.
	 * 
	 * @param typeString
	 * @return
	 */
	public static Type convertStringToType(String typeString) {
		Type type = null;
		if (typeString==null)
			return null;
		if (typeString.toLowerCase().startsWith("class "))
			typeString = typeString.substring(5).trim();
		if (typeString != null && typeString.equalsIgnoreCase("String.class"))
			type = String.class;
		else if (typeString != null && typeString.equalsIgnoreCase("Integer.class"))
			type = Integer.class;
		else if (typeString != null && typeString.equalsIgnoreCase("Double.class"))
			type = Double.class;
		else if (typeString != null && typeString.equalsIgnoreCase("Date.class"))
			type = Date.class;
		else {
			try {
				type = Class.forName(typeString);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return type;
	}

	/**
	 * Converts this TypedConstant into a string that can be parsed back to a
	 * TypedConstant Object.
	 * 
	 * @return
	 */
	public String serializeToString() {
		if (getValue() instanceof Date) {
			SimpleDateFormat sdfmt1 = new SimpleDateFormat("yyyy-MM-dd");
			return "_Typed" + getType() + "_" + sdfmt1.format(getValue());
		}
		return "_Typed" + getType() + "_" + getValue();
	}
}
