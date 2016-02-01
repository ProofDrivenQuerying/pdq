package uk.ac.ox.cs.pdq.io.pretty;

import java.io.PrintStream;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.fol.LogicalSymbols;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.io.Writer;

/**
 * Writes a concise representation of a dependency to the given output
 * 
 * @author Julien Leblay
 */
public class PrettyDependencyWriter extends PrettyWriter<Constraint> implements Writer<Constraint> {

	/**
	 * The default out to which dependencies should be written, if not 
	 * explicitly provided at write time.
	 */
	private PrintStream out;

	/** If true, the dependency atoms are indented. */
	private boolean indented = false;
	
	/**
	 * 
	 * @param out the default output
	 */
	private PrettyDependencyWriter(PrintStream out) {
		this.out = out;
	}
	
	/**
	 * Fluent pretty writer provider.
	 * @param out
	 * @return a new PrettyQueryWriter with the given default output.
	 */
	public static PrettyDependencyWriter to(PrintStream out) {
		return new PrettyDependencyWriter(out);
	}
	
	/**
	 * Fluent set to make the printer indented..
	 *
	 * @return this PrettyWriter after making it indented.
	 */
	public PrettyDependencyWriter indented() {
		this.indented = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.io.Writer#write(java.io.PrintStream, java.lang.Object)
	 */
	@Override
	public void write(PrintStream out, Constraint tgd) {
		String sep = "";
		for (Predicate f: tgd.getLeft().getPredicates()) {
			out.print(sep);
			out.print(f.getSignature().getName());
			out.print('(' + joinTerms(f.getTerms(), ", ") + ')');
			sep = (this.indented ? "\n" : " ") + LogicalSymbols.AND + ' ';
		}
		sep = (this.indented ? "\n\t" : " ")  + LogicalSymbols.IMPLIES + ' ';
		for (Predicate f : tgd.getRight().getPredicates()) {
			out.print(sep);
			out.print(f.getSignature().getName());
			out.print('(' + joinTerms(f.getTerms(), ", ") + ')');
			sep = (this.indented ? "\n\t" : " ") + LogicalSymbols.AND + ' ';
		}
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.io.pretty.PrettyWriter#write(java.lang.Object)
	 */
	@Override
	public void write(Constraint t) {
		this.write(this.out, t);
	}
}
