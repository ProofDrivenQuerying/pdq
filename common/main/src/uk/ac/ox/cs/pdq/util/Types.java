// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.util;

import java.lang.reflect.Type;

import org.apache.log4j.Logger;

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
			return type.getClass().getSimpleName();
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
			return type.getClass().getCanonicalName();
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
			return Number.class.isAssignableFrom(type.getClass());
		}
		return false;

	}

}
