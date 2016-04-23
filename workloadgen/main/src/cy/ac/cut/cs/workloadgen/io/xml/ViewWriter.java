package cy.ac.cut.cs.workloadgen.io.xml;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import uk.ac.ox.cs.pdq.io.xml.QNames;
import cy.ac.cut.cs.workloadgen.query.View;
import cy.ac.cut.cs.workloadgen.schema.Attribute;

/**
 * Writes relations to XML.
 * 
 * @author Julien Leblay
 */
public class ViewWriter {

	/**
	 * Writes a standard XML header to the output.
	 * @param out
	 */
	protected static void prolog(PrintStream out) {
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	}

	/**
	 * Writes an opening tag with the given QName to the given output
	 * @param out
	 * @param qname
	 */
	protected static void open(PrintStream out, QNames qname) {
		out.println("<" + qname.format() + ">");
	}
	
	/**
	 * Writes an opening tag with the given QName and attributes to the given 
	 * output
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
	 * Writes a stand-alone tag with the given QName to the given output
	 * @param out
	 * @param qname
	 */
	protected static void openclose(PrintStream out, QNames qname) {
		out.println('<' + qname.format() + "/>");
	}

	/**
	 * Writes a stand-alone tag with the given QName and attributes to the 
	 * given output.
	 * @param out
	 * @param qname
	 * @param properties Properties
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
	 * @param out
	 * @param qname
	 */
	protected static void close(PrintStream out, QNames qname) {
		out.println("</" + qname.format() + '>');
	}
	
	/**
	 * Writes the given relation to the given output.
	 * @param out
	 * @param relation
	 */
	public void writeRelation(PrintStream out, View relation) {
		Map<QNames, String> att = new LinkedHashMap<>();
		att.put(QNames.NAME, relation.getName());

		if (relation instanceof View) {
			open(out, QNames.VIEW, att);
		} else {
			open(out, QNames.RELATION, att);
		}
		// If the relation described externally, no need to specified the attributes
		for (Attribute a : relation.getSelectClause()) {
			this.writeAttribute(out, a);
		}
		if (relation instanceof View) {
			close(out, QNames.VIEW);
		} else {
			close(out, QNames.RELATION);
		}
	}
	
	/**
	 * Writes the given relation to the given output.
	 * @param out
	 * @param attribute
	 */
	public void writeAttribute(PrintStream out, Attribute attribute) {
		Map<QNames, String> att = new LinkedHashMap<>();
		att.put(QNames.NAME, attribute.getName());
		att.put(QNames.TYPE, Attribute.canonicalName(attribute.getType()));
		openclose(out, QNames.ATTRIBUTE, att);
	}


	/**
	 * @param out PrintStream
	 * @param o Relation
	 */
	public void write(PrintStream out, View o) {
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		this.writeRelation(out, o);
	}
}
