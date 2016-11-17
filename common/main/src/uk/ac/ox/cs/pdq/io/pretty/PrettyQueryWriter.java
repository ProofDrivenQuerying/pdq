package uk.ac.ox.cs.pdq.io.pretty;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.LogicalSymbols;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.io.Writer;

import com.google.common.base.Joiner;

// TODO: Auto-generated Javadoc
/**
 * Writes a concise representation of a query to the given output.
 *
 * @author Julien Leblay
 */
public class PrettyQueryWriter extends PrettyWriter<ConjunctiveQuery> implements Writer<ConjunctiveQuery> {

	/**
	 * The default out to which queries should be written, if not 
	 * explicitly provided at write time.
	 */
	private PrintStream out;

	/**
	 * Instantiates a new pretty query writer.
	 *
	 * @param out the default output
	 */
	PrettyQueryWriter(PrintStream out) {
		this.out = out;
	}

	/**
	 * Instantiates a new pretty query writer.
	 */
	public PrettyQueryWriter() {
		this(System.out);
	}
		
	/**
	 * Fluent pretty writer provider.
	 *
	 * @param out the out
	 * @return a new PrettyQueryWriter with the given default output.
	 */
	public static PrettyQueryWriter to(PrintStream out) {
		return new PrettyQueryWriter(out);
	}
	
	/**
	 * Write.
	 *
	 * @param out PrintStream
	 * @param q ConjunctiveQuery
	 */
	@Override
	public void write(PrintStream out, ConjunctiveQuery q) {
		out.print(q.getHead().getPredicate().getName());
		out.print('(' + Joiner.on(", ").join(q.getFreeVariables()) + ")\u2190");
		String sep = "";
		for (Atom a : q.getAtoms()) {
			out.print(sep);
			Atom f = a;
			out.print(f.getPredicate().getName());
			out.print('(' + Joiner.on(", ").join(f.getTerms()) + ')');
			sep = ' ' + LogicalSymbols.AND.toString() + ' ';
		}
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
	
	/**
	 * Returns a short String representation of the given dependency. This
	 * by-passes toString which is too verbose for non-debug purpose.
	 * 
	 * @param t ConjunctiveQuery
	 * @return a short String representation of the dependency.
	 */
	public static String convert(ConjunctiveQuery t) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		PrettyQueryWriter.to(ps).write(t);
		return baos.toString();
	}
}
