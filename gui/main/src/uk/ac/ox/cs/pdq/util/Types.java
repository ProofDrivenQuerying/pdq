package uk.ac.ox.cs.pdq.util;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.fol.TypedConstant;

// TODO: Auto-generated Javadoc
/**
 * Utility class for Type-related operations.
 *  
 * @author Julien Leblay
 */
public class Types {

	/**  The logger. */
	public static Logger log = Logger.getLogger(Types.class);

	/**
	 * Simple name.
	 *
	 * @param type the type
	 * @return the shortest know name of the given type.
	 */
	public static String simpleName(Type type) {
		if (type instanceof Class) {
			return ((Class) type).getSimpleName();
		}
		return type.toString();

	}

	/**
	 * Canonical name.
	 *
	 * @param type the type
	 * @return the canonical name of the given type. By default, toString() is
	 * used. If the type is a conventional class, then getCanonicalName is used,
	 * if it is a DataType, then getName() is used.
	 */
	public static String canonicalName(Type type) {
		if (type instanceof Class) {
			return ((Class) type).getCanonicalName();
		}
		return type.toString();

	}

	/**
	 * Equals.
	 *
	 * @param o1 the o1
	 * @param o2 the o2
	 * @return if o1 and o2 are the same type
	 */
	public static boolean equals(Type o1, Type o2) {
		if (o1 == null) {
			return o2 == null;
		}
		return o1.equals(o2);
	}

	/**
	 * Checks if is numeric.
	 *
	 * @param type the type
	 * @return true if the given is numeric, false otherwise
	 */
	public static boolean isNumeric(Type type) {
		if (type instanceof Class) {
			return Number.class.isAssignableFrom((Class) type);
		}
		return false;

	}

	/**
	 * Attempts to cast the given string to given class. If the class is not
	 * supported, the return object is the same as s.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param o the o
	 * @return a representation of s cast to the given class.
	 */
	public static <T> T cast(Type type, Object o) {
		if (o == null) {
			return null;
		}
		try {
			if (type instanceof Class) {
				String s = String.valueOf(o);
				Class<?> cl = (Class) type;
				if (Integer.class.equals(cl)) {
					return (T) (s.isEmpty() ? null : cl.cast(Integer.valueOf(s.trim())));
				} else if (Long.class.equals(cl)) {
					return (T) (s.isEmpty() ? null : cl.cast(Long.valueOf(s.trim())));
				} else if (Double.class.equals(cl)) {
					return (T) (s.isEmpty() ? null : cl.cast(Double.valueOf(s.trim())));
				} else if (BigDecimal.class.equals(cl)) {
					return (T) (s.isEmpty() ? null : cl.cast(BigDecimal.valueOf(Double.valueOf(s.trim()))));
				} else if (Boolean.class.equals(cl)) {
					return (T) (s.isEmpty() ? null : cl.cast(Boolean.valueOf(s.trim())));
				} else if (String.class.equals(cl)) {
					return (T) String.valueOf(o);
				} else if (Character.class.equals(cl)) {
					return (T) new Character(s.charAt(0));
				} else if (java.sql.Date.class.equals(cl)) {
					java.util.Date d = (java.util.Date) new SimpleDateFormat("dd/MM/yyyy 00:00:00").parseObject(s.trim());
					return (T) cl.cast(new Date(d.getTime()));
				} else if (java.util.Date.class.equals(cl)) {
					java.util.Date d = (java.util.Date) new SimpleDateFormat("dd/MM/yyyy 00:00:00").parseObject(s.trim());
					return (T) cl.cast(new Date(d.getTime()));
				}
			} else {
				throw new ClassCastException(o + " could not be cast to " + type);
			}
		} catch (ParseException|ClassCastException e) {
			try {
				String s = String.valueOf(o);
				Class<?> cl = (Class) type;
				if (java.util.Date.class.equals(cl)) {
					java.util.Date d;
					d = (java.util.Date) new SimpleDateFormat("dd/MM/yyyy 00:00:00").parseObject(s.trim());
					return (T) cl.cast(new Date(d.getTime()));
				}
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			log.error(e);
		} catch (NumberFormatException e) {
			log.error(e);
		}
		throw new ClassCastException(o + " could not be cast to " + type);
	}

	/**
	 * Make constant.
	 *
	 * @param <T> the generic type
	 * @param c the c
	 * @return the typed constant
	 */
	public static <T> TypedConstant makeConstant(T c) {
		return TypedConstant.create(c);
	}

	/**
	 * Make constant.
	 *
	 * @param <T> the generic type
	 * @param t the t
	 * @param o the o
	 * @return the typed constant
	 */
	public static <T> TypedConstant makeConstant(Type t, Object o) {
		return TypedConstant.create((T) cast(t, o));
	}
}
