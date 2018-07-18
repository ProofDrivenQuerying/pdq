package uk.ac.ox.cs.pdq.datasources.legacy.io.xml;

import java.io.PrintStream;
import java.util.Map;
import java.util.Properties;

import uk.ac.ox.cs.pdq.io.Writer;

/**
 * Mostly unused, outdated by jaxb IO implementation.
 *
 * @author Julien Leblay
 * @param <T> the generic type
 */
public abstract class AbstractXMLWriter<T> implements Writer<T> {

	/**
	 * Writes a standard XML header to the output.
	 *
	 * @param out 
	 */
	protected static void prolog(PrintStream out) {
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	}

	/**
	 * Writes an opening tag with the given QName to the given output.
	 *
	 * @param out 
	 * @param qname 
	 */
	protected static void open(PrintStream out, QNames qname) {
		out.println("<" + qname.format() + ">");
	}
	
	/**
	 * Writes an opening tag with the given QName and attributes to the given 
	 * output.
	 *
	 * @param out 
	 * @param qname 
	 * @param attributes 
	 */
	protected static void open(PrintStream out, QNames qname,
			Map<QNames, String> attributes) {
		out.print('<' + qname.format());
		for (QNames att : attributes.keySet()) {
			out.print(' ' + att.format() + "=\"");
			out.print(attributes.get(att));
			out.print('"');
		}
		out.println('>');
	}

	/**
	 * Writes a stand-alone tag with the given QName to the given output.
	 *
	 * @param out 
	 * @param qname 
	 */
	protected static void openclose(PrintStream out, QNames qname) {
		out.println('<' + qname.format() + "/>");
	}

	/**
	 * Writes a stand-alone tag with the given QName and attributes to the 
	 * given output.
	 *
	 * @param out 
	 * @param qname 
	 * @param properties 
	 */
	protected static void openclose(PrintStream out, QNames qname,
			Properties properties) {
		out.print('<' + qname.format());
		for (Object att : properties.keySet()) {
			out.print(" " + att + "=\"");
			out.print(properties.get(att));
			out.print('"');
		}
		out.println("/>");
	}

	/**
	 * Writes a stand-alone tag with the given QName and attributes to the 
	 * given output.
	 * Same as the previous, but it getts the properties as an attribute map.
	 * @param out 
	 * @param qname 
	 * @param attributes 
	 */
	protected static void openclose(PrintStream out, QNames qname,
			Map<QNames, String> attributes) {
		out.print('<' + qname.format());
		for (QNames att : attributes.keySet()) {
			out.print(' ' + att.format() + "=\"");
			out.print(attributes.get(att));
			out.print('"');
		}
		out.println("/>");
	}

	/**
	 * Writes a closing tag with the given QName to the given output.
	 *
	 * @param out 
	 * @param qname 
	 */
	protected static void close(PrintStream out, QNames qname) {
		out.println("</" + qname.format() + '>');
	}
}
