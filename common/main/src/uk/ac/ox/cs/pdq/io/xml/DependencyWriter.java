package uk.ac.ox.cs.pdq.io.xml;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * Writes dependencies to XML.
 * 
 * @author Julien Leblay
 */
public class DependencyWriter extends AbstractXMLWriter<Dependency> {

	/**
	 * Writes the given relation to the given output.
	 *
	 * @param out the output stream being written to
	 * @param dep the TGD being written
	 */
	public void writeDependency(PrintStream out, Dependency dep) {
		Map<QNames, String> att = new LinkedHashMap<>();
		open(out, QNames.DEPENDENCY, att);
		open(out, QNames.BODY, att);
		for (Atom a: dep.getLeft().getAtoms()) {
			this.writePredicate(out, a);
		}
		close(out, QNames.BODY);
		open(out, QNames.HEAD, att);
		for (Atom a: dep.getRight().getAtoms()) {
			this.writePredicate(out, a);
		}
		close(out, QNames.HEAD);
		close(out, QNames.DEPENDENCY);
	}

	/**
	 * Writes the given relation to the given output.
	 *
	 * @param out the output streem being written to 
	 * @param a Atom being written
	 */
	public void writePredicate(PrintStream out, Atom a) {
		Atom p = a;
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
	 * Write the dependency to the output stream.
	 *
	 * @param out PrintStream
	 * @param o TGD
	 */
	@Override
	public void write(PrintStream out, Dependency o) {
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		this.writeDependency(out, o);
	}
}
