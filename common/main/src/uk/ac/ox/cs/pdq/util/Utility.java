// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.util;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Term;

/**
 * Provide utility function, that don't fit anywhere else.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 * @author George Konstantinidis
 */
public class Utility {

	/** The logger. */
	public static Logger log = Logger.getLogger(Utility.class);

	/**
	 * Search.
	 *
	 * @param            <T> the generic type
	 * @param collection the collection
	 * @param object     the object
	 * @return the positions where the input object appears in collection. If object
	 *         does not appear in source, then an empty list is returned
	 */
	public static <T> List<Integer> search(T[] collection, T object) {
		List<Integer> result = new ArrayList<>();
		int index = 0;
		for (T obj : collection) {
			if (obj.equals(object)) {
				result.add(index);
			}
			index++;
		}
		return result;
	}

	public static Set<Constant> getUntypedConstants(Collection<Atom> atoms) {
		Set<Constant> result = new LinkedHashSet<>();
		for (Atom atom : atoms) {
			for (Term term : atom.getTerms()) {
				if (term.isUntypedConstant())
					result.add((Constant) term);
			}
		}
		return result;
	}

	/**
	 * Attempts to cast the given string to given class. If the class is not
	 * supported, the return object is the same as s.
	 *
	 * @param      <T> the generic type
	 * @param type the type
	 * @param o    the o
	 * @return a representation of s cast to the given class.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T cast(Type type, Object o) {
		if (o == null) {
			return null;
		}
		try {
			if (type instanceof Class) {
				String s = String.valueOf(o);
				Class<?> cl = (Class<?>) type;
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
				} else if (Date.class.equals(cl)) {
					java.util.Date d = (java.util.Date) new SimpleDateFormat("yyyy-MM-dd").parseObject(s.trim());
					return (T) cl.cast(new Date(d.getTime()));
				}
			} else {
				throw new ClassCastException(o + " could not be cast to " + type);
			}
		} catch (NumberFormatException e) {
			log.error(e);
		} catch (ParseException e) {
			log.error(e);
		}
		throw new ClassCastException(o + " could not be cast to " + type);
	}

}
