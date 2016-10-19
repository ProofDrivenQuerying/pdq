package uk.ac.ox.cs.pdq.io.pretty;

import java.io.PrintStream;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.io.Writer;

// TODO: Auto-generated Javadoc
/**
 * Writes a concise representation of a dependency to the given output.
 *
 * @author Julien Leblay
 */
public class VeryPrettyDependencyWriter 
		extends PrettyWriter<Dependency> implements Writer<Dependency> {

	/** The Constant AND. */
	public static final String AND = " \u2227 ";
	
	/** The Constant IMPLIES. */
	public static final String IMPLIES = " \u21D2 ";
	
	/**
	 * The default out to which dependencies should be written, if not 
	 * explicitly provided at write time.
	 */
	private PrintStream out;

	/** If true, the dependency atoms are indented. */
	private boolean indented = false;
	
	/**
	 * Instantiates a new very pretty dependency writer.
	 *
	 * @param out the default output
	 */
	private VeryPrettyDependencyWriter(PrintStream out) {
		this.out = out;
	}
	
	/**
	 * Fluent pretty writer provider.
	 *
	 * @param out the out
	 * @return a new PrettyQueryWriter with the given default output.
	 */
	public static VeryPrettyDependencyWriter to(PrintStream out) {
		return new VeryPrettyDependencyWriter(out);
	}
	
	/**
	 * Fluent set to make the printer indented..
	 *
	 * @return this PrettyWriter after making it indented.
	 */
	public VeryPrettyDependencyWriter indented() {
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
		int length = 0;
		for (Atom f: tgd.getLeft().getAtoms()) {
			out.print(sep);
			out.print(f.getPredicate().getName());
			out.print('(' + joinTerms(f.getTerms(), ", ") + ')');
			if (this.indented && length > 80) {
				sep = "\n" + AND + ' ';
				length = 0;
			} else {
				sep = " " + AND + ' ';
			}
			length += String.valueOf(f).length();
		}
		length = 0;
		sep = (this.indented ? "\n\t" : " ")  + IMPLIES + ' ';
		for (Atom f : tgd.getRight().getAtoms()) {
			out.print(sep);
			out.print(f.getPredicate().getName());
			out.print('(' + joinTerms(f.getTerms(), ", ") + ')');
			if (this.indented && length > 80) {
				sep = "\n\t" + AND + ' ';
				length = 0;
			} else {
				sep = " " + AND + ' ';
			}
			length += String.valueOf(f).length();
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
