package uk.ac.ox.cs.pdq.io.pretty;

import java.io.PrintStream;

import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.LogicalSymbols;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.io.Writer;

import com.google.common.base.Joiner;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

// TODO: Auto-generated Javadoc
/**
 * Writes a concise representation of a query to the given output.
 *
 * @author Julien Leblay
 */
public class VeryPrettyQueryWriter extends PrettyWriter<ConjunctiveQuery> implements Writer<ConjunctiveQuery> {

	/**
	 * The default out to which queries should be written, if not 
	 * explicitly provided at write time.
	 */
	private PrintStream out;

	/**
	 * Instantiates a new very pretty query writer.
	 *
	 * @param out the default output
	 */
	VeryPrettyQueryWriter(PrintStream out) {
		this.out = out;
	}

	/**
	 * Instantiates a new very pretty query writer.
	 */
	VeryPrettyQueryWriter() {
		this(System.out);
	}
		
	/**
	 * Fluent pretty writer provider.
	 *
	 * @param out the out
	 * @return a new PrettyQueryWriter with the given default output.
	 */
	public static VeryPrettyQueryWriter to(PrintStream out) {
		return new VeryPrettyQueryWriter(out);
	}
	
	/**
	 * Write.
	 *
	 * @param out PrintStream
	 * @param q ConjunctiveQuery
	 */
	@Override
	public void write(PrintStream out, ConjunctiveQuery q) {
		out.print(q.getHead().getSignature().getName());
		out.print('(' + Joiner.on(", ").join(q.getFree()) + ")\n\t<-");
		String sep = "";
		Multimap<Term, Atom> clusters = LinkedHashMultimap.create();
		for (Atom a : q.getBody()) {
			for (Term t: a.getTerms()) {
				clusters.put(t, a);
			}
		}
		for (Atom a : q.getBody()) {
			out.print(sep);
			out.print(a.getSignature().getName());
			String sep2 = "(";
			for (Term t: a.getTerms()) {
				out.print(sep2 + t);
				if (clusters.get(t).size() > 1) {
					out.print('*');
				}
				sep2 = ", ";
			}
			out.print(')');
			sep = "\n\t" + LogicalSymbols.AND.toString() + ' ';
		}
		out.print('\n');
	}
	
	/**
	 * Write.
	 *
	 * @param q ConjunctiveQuery
	 */
	@Override
	public void write(ConjunctiveQuery q) {
		this.write(this.out, q);
	}
}
