package uk.ac.ox.cs.pdq.io;

import java.io.PrintStream;

/**
 * Writes experiment sample elements to XML.
 *
 * @author Julien Leblay
 */
public interface Writer<T> {

	/**
	 * Writes the given object to the given output using an XML syntax.
	 * @param out
	 * @param o
	 */
	void write(PrintStream out, T o);
}
