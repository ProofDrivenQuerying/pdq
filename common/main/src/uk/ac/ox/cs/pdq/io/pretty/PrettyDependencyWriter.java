package uk.ac.ox.cs.pdq.io.pretty;

import java.io.PrintStream;

import uk.ac.ox.cs.pdq.fol.LogicalSymbols;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.io.Writer;

// TODO: Auto-generated Javadoc
/**
 * Writes a concise representation of a dependency to the given output.
 *
 * @author Julien Leblay
 */
public class PrettyDependencyWriter extends PrettyWriter<Dependency> implements Writer<Dependency> {

	/**
	 * The default out to which dependencies should be written, if not 
	 * explicitly provided at write time.
	 */
	private PrintStream out;

	/** If true, the dependency atoms are indented. */
	private boolean indented = false;
	
	/**
	 * Instantiates a new pretty dependency writer.
	 *
	 * @param out the default output
	 */
	private PrettyDependencyWriter(PrintStream out) {
		this.out = out;
	}
	
	/**
	 * Fluent pretty writer provider.
	 *
	 * @param out the out
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
	public void write(PrintStream out, Dependency tgd) {
		String sep = "";
		for (Atom f: tgd.getBody().getAtoms()) {
			out.print(sep);
			out.print(f.getPredicate().getName());
			out.print('(' + joinTerms(f.getTerms(), ", ") + ')');
			sep = (this.indented ? "\n" : " ") + LogicalSymbols.AND + ' ';
		}
		sep = (this.indented ? "\n\t" : " ")  + LogicalSymbols.IMPLIES + ' ';
		for (Atom f : tgd.getHead().getAtoms()) {
			out.print(sep);
			out.print(f.getPredicate().getName());
			out.print('(' + joinTerms(f.getTerms(), ", ") + ')');
			sep = (this.indented ? "\n\t" : " ") + LogicalSymbols.AND + ' ';
		}
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.io.pretty.PrettyWriter#write(java.lang.Object)
	 */
	@Override
	public void write(Dependency t) {
		this.write(this.out, t);
	}
}
