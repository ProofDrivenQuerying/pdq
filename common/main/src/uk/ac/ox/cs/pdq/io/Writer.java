package uk.ac.ox.cs.pdq.io;

import java.io.PrintStream;

// TODO: Auto-generated Javadoc
/**
 * Writes experiment sample elements to XML.
 *
 * @author Julien Leblay
 * @param <T> the generic type
 */
public interface Writer<T> {

	/**
	 * Writes the given object to the given output using an XML syntax.
	 *
	 * @param out the out
	 * @param o the o
	 */
	void write(PrintStream out, T o);
}
