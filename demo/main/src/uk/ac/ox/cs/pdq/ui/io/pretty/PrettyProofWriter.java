package uk.ac.ox.cs.pdq.ui.io.pretty;

import java.io.PrintStream;
import java.util.Map;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.Writer;
import uk.ac.ox.cs.pdq.io.pretty.PrettyFormulaWriter;
import uk.ac.ox.cs.pdq.io.pretty.PrettyWriter;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.ui.proof.Proof;
import uk.ac.ox.cs.pdq.ui.proof.Proof.State;

// TODO: Auto-generated Javadoc
/**
 * Writes a concise representation of a query to the given output.
 *
 * @author Julien Leblay
 */
public class PrettyProofWriter extends PrettyWriter<Proof> implements Writer<Proof> {

	/**
	 * The default out to which queries should be written, if not 
	 * explicitly provided at write time.
	 */
	private PrintStream out;

	/**
	 * Instantiates a new pretty proof writer.
	 *
	 * @param out the default output
	 */
	private PrettyProofWriter(PrintStream out) {
		this.out = out;
	}
	
	/**
	 * Fluent pretty writer provider.
	 *
	 * @param out the out
	 * @return a new PrettyQueryWriter with the given default output.
	 */
	public static PrettyProofWriter to(PrintStream out) {
		return new PrettyProofWriter(out);
	}
	
	/**
	 * Write.
	 *
	 * @param out PrintStream
	 * @param p Proof
	 */
	@Override
	public void write(PrintStream out, Proof p) {
		String sep = "";
		for (State state: p.getStates()) {
			AccessibilityAxiom axiom = state.getAxiom();
			out.print("Axiom: ");
			out.println(axiom.getBaseRelation().getName() + '/' + axiom.getAccessMethod().getName());
			
			PrettyFormulaWriter dw = PrettyFormulaWriter.to(out).indented();
			out.print("Groundings: ");
			for (Map<Variable, Constant> candidate: state.getMatches()) {
				out.print('\n');
				dw.write(axiom.fire(candidate,true));
				out.println();
			}
			out.println();
		}
	}
	
	/**
	 * Write.
	 *
	 * @param q Proof
	 */
	@Override
	public void write(Proof q) {
		this.write(this.out, q);
	}
}
