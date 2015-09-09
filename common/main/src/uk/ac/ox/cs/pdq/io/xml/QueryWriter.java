package uk.ac.ox.cs.pdq.io.xml;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.ox.cs.pdq.fol.AcyclicQuery;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Predicate;
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
	 * @param out
	 * @param query ConjunctiveQuery
	 */
	private void writeQuery(PrintStream out, ConjunctiveQuery query) {
		Map<QNames, String> att = new LinkedHashMap<>();
		if (query instanceof AcyclicQuery) {
			att.put(QNames.TYPE, "acyclic");
		} else {
			att.put(QNames.TYPE, "conjunctive");
		}
		open(out, QNames.QUERY, att);
		this.writeBody(out, query.getBody());
		this.writeHead(out, query.getHead());
		close(out, QNames.QUERY);
	}

	/**
	 * Writes the given query to the given output.
	 * @param out
	 * @param query ConjunctiveQuery
	 * @param atts Map<QNames,String>
	 */
	private void writeQuery(PrintStream out, ConjunctiveQuery query, Map<QNames, String> atts) {
		Map<QNames, String> att = new LinkedHashMap<>(atts);
		if (query instanceof AcyclicQuery) {
			att.put(QNames.TYPE, "acyclic");
		} else {
			att.put(QNames.TYPE, "conjunctive");
		}
		open(out, QNames.QUERY, att);
		this.writeBody(out, query.getBody());
		this.writeHead(out, query.getHead());
		close(out, QNames.QUERY);
	}

	/**
	 * Writes the given body to the given output.
	 * @param out
	 * @param body Conjunction<PredicateFormula>
	 */
	public void writeBody(PrintStream out, Conjunction<Predicate> body) {
		Map<QNames, String> att = new LinkedHashMap<>();
		open(out, QNames.BODY, att);
		for (Predicate a: body) {
			this.writePredicate(out, a);
		}
		close(out, QNames.BODY);
	}

	/**
	 * Writes the given predicate to the given output.
	 * @param out
	 * @param p PredicateFormula
	 */
	public void writePredicate(PrintStream out, Predicate p) {
		Map<QNames, String> att = new LinkedHashMap<>();
		att.put(QNames.NAME, p.getName());
		open(out, QNames.ATOM, att);
		for (Term t: p.getTerms()) {
			Map<QNames, String> att2 = new LinkedHashMap<>();
			if (t.isVariable()) {
				att2.put(QNames.NAME, ((Variable) t).getName());
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
	 * @param out
	 * @param p PredicateFormula
	 */
	public void writeHead(PrintStream out, Predicate p) {
		Map<QNames, String> att = new LinkedHashMap<>();
		att.put(QNames.NAME, p.getName());
		open(out, QNames.HEAD, att);
		for (Term t: p.getTerms()) {
			Map<QNames, String> att2 = new LinkedHashMap<>();
			if (t.isVariable()) {
				att2.put(QNames.NAME, ((Variable) t).getName());
				openclose(out, QNames.VARIABLE, att2);
			} else {
				att2.put(QNames.VALUE, t.toString());
				openclose(out, QNames.CONSTANT, att2);
			}
		}
		close(out, QNames.HEAD);
	}

	/**
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
	 * element
	 * @param out
	 * @param o
	 * @param atts
	 */
	public void write(PrintStream out, ConjunctiveQuery o, Map<QNames, String> atts) {
		this.writeQuery(out, o, atts);
	}
}
