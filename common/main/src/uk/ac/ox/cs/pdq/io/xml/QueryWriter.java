package uk.ac.ox.cs.pdq.io.xml;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * Writes queries to XML.
 * 
 * @author Julien Leblay
 */
public class QueryWriter extends AbstractXMLWriter<ConjunctiveQuery> {

	/**
	 * Writes the given query to the given output.
	 *
	 * @param out the out
	 * @param query ConjunctiveQuery
	 */
	private void writeQuery(PrintStream out, ConjunctiveQuery query) {
		Map<QNames, String> att = new LinkedHashMap<>();
		att.put(QNames.TYPE, "conjunctive");
		open(out, QNames.QUERY, att);
		this.writeBody(out, query.getChildren()[0]);
		this.writeHead(out, query.getFreeVariables());
		close(out, QNames.QUERY);
	}

	/**
	 * Writes the given query to the given output.
	 *
	 * @param out the out
	 * @param query ConjunctiveQuery
	 * @param atts Map<QNames,String>
	 */
	private void writeQuery(PrintStream out, ConjunctiveQuery query, Map<QNames, String> atts) {
		Map<QNames, String> att = new LinkedHashMap<>(atts);
		att.put(QNames.TYPE, "conjunctive");
		open(out, QNames.QUERY, att);
		this.writeBody(out, query.getChildren()[0]);
		this.writeHead(out, query.getFreeVariables());
		close(out, QNames.QUERY);
	}

	/**
	 * Writes the given body to the given output.
	 *
	 * @param out the out
	 * @param body Conjunction<Atom>
	 */
	public void writeBody(PrintStream out, Formula body) {
		Map<QNames, String> att = new LinkedHashMap<>();
		open(out, QNames.BODY, att);
		for (Atom a: body.getAtoms()) {
			this.writePredicate(out, a);
		}
		close(out, QNames.BODY);
	}

	/**
	 * Writes the given predicate to the given output.
	 *
	 * @param out the out
	 * @param p Atom
	 */
	public void writePredicate(PrintStream out, Atom p) {
		Map<QNames, String> att = new LinkedHashMap<>();
		att.put(QNames.NAME, p.getPredicate().getName());
		open(out, QNames.ATOM, att);
		for (Term t: p.getTerms()) {
			Map<QNames, String> att2 = new LinkedHashMap<>();
			if (t.isVariable()) {
				att2.put(QNames.NAME, ((Variable) t).getSymbol());
				openclose(out, QNames.VARIABLE, att2);
			} else {
				att2.put(QNames.VALUE, t.toString());
				openclose(out, QNames.CONSTANT, att2);
			}
		}
		close(out, QNames.ATOM);
	}

	/**
	 * Writes the given head to the given output.
	 *
	 * @param out the out
	 * @param p Atom
	 */
	public void writeHead(PrintStream out, Variable[] variables) {
		Map<QNames, String> att = new LinkedHashMap<>();
		att.put(QNames.NAME, "Q");
		open(out, QNames.HEAD, att);
		for (Variable t:variables) {
			Map<QNames, String> att2 = new LinkedHashMap<>();
			att2.put(QNames.NAME, ((Variable) t).getSymbol());
			openclose(out, QNames.VARIABLE, att2);
		}
		close(out, QNames.HEAD);
	}

	/**
	 * Write.
	 *
	 * @param out PrintStream
	 * @param o ConjunctiveQuery
	 */
	@Override
	public void write(PrintStream out, ConjunctiveQuery o) {
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		this.writeQuery(out, o);
	}

	/**
	 * Writes query to the given stream with some additional attributes in the root
	 * element.
	 *
	 * @param out the out
	 * @param o the o
	 * @param atts the atts
	 */
	public void write(PrintStream out, ConjunctiveQuery o, Map<QNames, String> atts) {
		this.writeQuery(out, o, atts);
	}
}
