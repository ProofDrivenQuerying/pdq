package uk.ac.ox.cs.pdq.io.pretty;

import java.io.PrintStream;

import uk.ac.ox.cs.pdq.fol.BinaryFormula;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.NaryFormula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.UnaryFormula;
import uk.ac.ox.cs.pdq.io.Writer;

// TODO: Auto-generated Javadoc
/**
 * Writes a concise representation of a dependency to the given output.
 *
 * @author Julien Leblay
 * @param <T> the generic type
 */
public class PrettyFormulaWriter<T extends Formula> extends PrettyWriter<T> implements Writer<T> {

	/**
	 * The default out to which dependencies should be written, if not 
	 * explicitly provided at write time.
	 */
	private PrintStream out;

	/** If true, the dependency atoms are indented. */
	private boolean indented = false;
	
	/**
	 * Instantiates a new pretty formula writer.
	 *
	 * @param out the default output
	 */
	private PrettyFormulaWriter(PrintStream out) {
		this.out = out;
	}
	
	/**
	 * Fluent pretty writer provider.
	 *
	 * @param out the out
	 * @return a new PrettyQueryWriter with the given default output.
	 */
	public static PrettyFormulaWriter to(PrintStream out) {
		return new PrettyFormulaWriter(out);
	}
	
	/**
	 * Fluent set to make the printer indented..
	 * @return this PrettyWriter after making it indented.
	 */
	public PrettyFormulaWriter<T> indented() {
		this.indented = true;
		return this;
	}

	/**
	 * Write.
	 *
	 * @param out PrintStream
	 * @param formula Formula
	 */
	@Override
	public void write(PrintStream out, Formula formula) {
		if (formula instanceof NaryFormula) {
			this.write(out, (NaryFormula<?>) formula);
			return;
		}
		if (formula instanceof BinaryFormula) {
			this.write(out, (BinaryFormula<?, ?>) formula);
			return;
		}
		if (formula instanceof UnaryFormula) {
			this.write(out, (UnaryFormula<?>) formula);
			return;
		}
		if (formula instanceof Predicate) {
			this.write(out, (Predicate) formula);
			return;
		}
		throw new UnsupportedOperationException("No writer operation defined for " + formula);
	}

	/**
	 * Write.
	 *
	 * @param out PrintStream
	 * @param formula NaryFormula<Formula>
	 */
	public void write(PrintStream out, NaryFormula<Formula> formula) {
		String sep = "";
		for (Formula f: formula.getChildren()) {
			out.print(sep);
			this.write(out, f);
			sep = (this.indented ? "\n\t" : " ") + formula.getSymbol() + ' ';
		}
	}

	/**
	 * Write.
	 *
	 * @param out PrintStream
	 * @param formula BinaryFormula<Formula,Formula>
	 */
	public void write(PrintStream out, BinaryFormula<Formula, Formula> formula) {
		// Do not indent the left hand side
		boolean i = this.indented;
		this.indented = false;
		this.write(out, formula.getLeft());
		this.indented = i;
		
		out.print((this.indented ? "\n\t" : " ") + formula.getSymbol() + ' ');
		this.write(out, formula.getRight());
	}

	/**
	 * Write.
	 *
	 * @param out PrintStream
	 * @param formula UnaryFormula<Formula>
	 */
	public void write(PrintStream out, UnaryFormula<Formula> formula) {
		out.print(formula.getSymbol());
		Formula sub = formula.getChild();
		out.print('(');
		this.write(out, sub);
		out.print('(');
	}

	/**
	 * Write.
	 *
	 * @param out PrintStream
	 * @param formula AtomicFormula
	 */
	public void write(PrintStream out, Predicate formula) {
		out.print(formula.getSignature().getName());
		out.print('(' + joinTerms(formula.getTerms(), ", ") + ')');
	}

	/**
	 * Write.
	 *
	 * @param t Formula
	 */
	@Override
	public void write(Formula t) {
		this.write(this.out, t);
	}
}
