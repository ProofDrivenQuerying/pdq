package formats;

import java.io.PrintWriter;

import uk.ac.ox.cs.pdq.fol.Predicate;

public class VampireFormulaFormat {

	protected final PrintWriter m_output;

	public VampireFormulaFormat(PrintWriter output) {
		this.m_output = output;
	}

	public void printPredicate(Predicate predicate, String place) {
		m_output.print("vampire(symbol, predicate, " + predicate.getName().toLowerCase() + ", " + new Integer(predicate.getArity()).toString() + ", " + place + "). ");
	}
}
