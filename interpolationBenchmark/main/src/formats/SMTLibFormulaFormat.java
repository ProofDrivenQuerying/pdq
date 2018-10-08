package formats;

import java.io.PrintWriter;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Clause;
import uk.ac.ox.cs.pdq.fol.Literal;
import uk.ac.ox.cs.pdq.fol.Predicate;

import com.google.common.base.Preconditions;

public class SMTLibFormulaFormat {

	protected final PrintWriter m_output;

	public SMTLibFormulaFormat(PrintWriter output) {
		this.m_output = output;
	}

	public void printPredicate(Predicate predicate, String predicatePrefix) {
		m_output.print("(" + "declare-fun " + predicatePrefix + predicate.getName().toLowerCase() + " () Bool" + ")");
	}

	public void printCNFFormula(Clause formula, String predicatePrefix) {
		m_output.print("(" + "assert " + processClause(formula, predicatePrefix) + ")");
	}
	
	public void printNegatedCNFFormula(Clause formula, String predicatePrefix) {
		m_output.print("(" + "assert " + "(not " + processClause(formula, predicatePrefix) + ")" + ")");
	}


	private String processClause(Clause formula, String predicatePrefix) {
		String outputString = "";
		if(formula.getLiterals().size() > 1) {
			outputString = "(or ";
			int i = 0;
			for(Literal literal:formula.getLiterals()) {
				outputString += processLiteral(literal, predicatePrefix);
				if(i < ((Clause)formula).getLiterals().size() - 1) {
					outputString += " ";
				}
				++i;
			}	
			outputString += ")";
		}
		else {
			outputString += processLiteral(formula.getLiterals().iterator().next(), predicatePrefix);
		}
		return outputString;
	}

	private String processLiteral(Literal literal, String predicatePrefix) {
		String outputString = "";
		if(literal.isPositive()) {
			outputString += processAtom(literal, predicatePrefix);
		}
		else {
			outputString += "(" + "not " + processAtom(literal, predicatePrefix) + ")";
		}
		return outputString;
	}

	private String processAtom(Atom atom, String predicatePrefix) {
		String outputString = predicatePrefix + atom.getPredicate().getName().toLowerCase();
		Preconditions.checkArgument(atom.getTerms().size() == 0, "Non propositional formulas are not accepted");
		return outputString;
	}	

}
