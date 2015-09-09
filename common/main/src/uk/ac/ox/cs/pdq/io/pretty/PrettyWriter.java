package uk.ac.ox.cs.pdq.io.pretty;

import java.util.Collection;

import uk.ac.ox.cs.pdq.fol.Term;

/**
 * Writes a concise representation of a query to the given output
 * 
 * @author Julien Leblay
 *
 */
public abstract class PrettyWriter<T> {

	/**
	 * Fluent interface to pretty writer. Writes the proper out. 
	 * @param t T
	 */
	public abstract void write(T t);
	
	/**
	 * 
	 * @param terms
	 * @param separator
	 * @return a String in which all the provided terms are separated with the
	 * given separators, and non-variable terms are surrounded with quotes.
	 */
	protected static String joinTerms(Collection<? extends Term> terms, String separator) {
		StringBuilder result = new StringBuilder();
		String s = "";
		for (Term t: terms) {
			result.append(s);
			if (t.isVariable()) {
				result.append(t);
			} else {
				result.append("'" + t + "'");
			}
			s = separator;
		}
		return result.toString();
	}
}
