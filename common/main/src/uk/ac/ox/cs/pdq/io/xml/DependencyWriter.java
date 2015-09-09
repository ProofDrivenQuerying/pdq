package uk.ac.ox.cs.pdq.io.xml;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * Writes dependencies to XML.
 * 
 * @author Julien Leblay
 */
public class DependencyWriter extends AbstractXMLWriter<Constraint> {

	/**
	 * Writes the given relation to the given output.
	 * @param out
	 * @param dep TGD
	 */
	public void writeDependency(PrintStream out, Constraint dep) {
		Map<QNames, String> att = new LinkedHashMap<>();
		open(out, QNames.DEPENDENCY, att);
		open(out, QNames.BODY, att);
		for (Predicate a: dep.getLeft().getPredicates()) {
			this.writePredicate(out, a);
		}
		close(out, QNames.BODY);
		open(out, QNames.HEAD, att);
		for (Predicate a: dep.getRight().getPredicates()) {
			this.writePredicate(out, a);
		}
		close(out, QNames.HEAD);
		close(out, QNames.DEPENDENCY);
	}

	/**
	 * Writes the given relation to the given output.
	 * @param out
	 * @param a PredicateFormula
	 */
	public void writePredicate(PrintStream out, Predicate a) {
		Predicate p = a;
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
 	 * @param out PrintStream
	 * @param o TGD
	 */
	@Override
	public void write(PrintStream out, Constraint o) {
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		this.writeDependency(out, o);
	}
}
