package uk.ac.ox.cs.pdq.io.pretty;

import java.io.PrintStream;

import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Implication;
import uk.ac.ox.cs.pdq.fol.LogicalSymbols;
import uk.ac.ox.cs.pdq.fol.Negation;
import uk.ac.ox.cs.pdq.io.Writer;

// TODO: Auto-generated Javadoc
/**
 * Writes a concise representation of a dependency to the given output.
 *
 * @author Julien Leblay
 * @param <T> the generic type
 */
public class PrettyFormulaWriter extends PrettyWriter<Formula> implements Writer<Formula> {

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
	public PrettyFormulaWriter indented() {
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
		if (formula instanceof Disjunction || formula instanceof Conjunction || formula instanceof Implication) {
			this.write(out, formula.getChildren().get(0));
			if(formula instanceof Disjunction) {
				out.print((this.indented ? "\n\t" : " ") + LogicalSymbols.OR + ' ');
			}
			else if(formula instanceof Conjunction) {
				out.print((this.indented ? "\n\t" : " ") + LogicalSymbols.AND + ' ');
			}
			else if(formula instanceof Implication) {
				out.print((this.indented ? "\n\t" : " ") + LogicalSymbols.IMPLIES + ' ');
			}
			this.write(out, formula.getChildren().get(1));
			return;
		}
		if (formula instanceof Negation) {
			out.print((this.indented ? "\n\t" : " ") + LogicalSymbols.NEGATION + ' ');
			this.write(out, formula.getChildren().get(0));
			return;
		}
		if (formula instanceof Atom) {
			out.print(((Atom)formula).getPredicate().getName());
			out.print('(' + joinTerms(formula.getTerms(), ", ") + ')');
			return;
		}
		throw new UnsupportedOperationException("No writer operation defined for " + formula);
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
