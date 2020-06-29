// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.xml;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.QNames;

// TODO: Auto-generated Javadoc
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
		if(query != null)
		{
			this.writeBody(out, query.getBody());
			Variable[] freeVars = query.getFreeVariables();
			this.writeHead(out, Atom.create(Predicate.create("Q", freeVars.length), freeVars));
		}
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
		if(query != null)
		{
			this.writeBody(out, query.getBody());
			Variable[] freeVars = query.getFreeVariables();
			this.writeHead(out, Atom.create(Predicate.create("Q", freeVars.length), freeVars));
		}
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
				att2.put(QNames.NAME, ((Variable) t).toString());
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
	public void writeHead(PrintStream out, Atom p) {
		Map<QNames, String> att = new LinkedHashMap<>();
		att.put(QNames.NAME, toString(p));
		open(out, QNames.HEAD, att);
		for (Term t: p.getTerms()) {
			Map<QNames, String> att2 = new LinkedHashMap<>();
			if (t.isVariable()) {
				att2.put(QNames.NAME, ((Variable) t).toString());
				openclose(out, QNames.VARIABLE, att2);
			} else {
				att2.put(QNames.VALUE, t.toString());
				openclose(out, QNames.CONSTANT, att2);
			}
		}
		close(out, QNames.HEAD);
	}

	/**
	 * Does the equivalent to Atom.toString() without the variables
	 */
	public String toString(Atom atom)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(atom.getPredicate().getName());
		return builder.toString();
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
