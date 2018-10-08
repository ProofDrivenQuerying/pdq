package formats;

import java.io.PrintWriter;
import java.util.List;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Clause;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.FunctionTerm;
import uk.ac.ox.cs.pdq.fol.Implication;
import uk.ac.ox.cs.pdq.fol.Literal;
import uk.ac.ox.cs.pdq.fol.LogicalSymbols;
import uk.ac.ox.cs.pdq.fol.Negation;
import uk.ac.ox.cs.pdq.fol.QuantifiedFormula;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;

import com.google.common.base.Preconditions;

public class TPTPFormulaFormat {

	private int axiomCounter = 0;
	private int hypothesisCounter = 0;
	private int negatedConjectureCounter = 0;

	public static enum TPTPFormulaRole {
		AXIOM,
		HYPOTHESIS,
		CONJECTURE,
	};

	protected final PrintWriter m_output;

	public TPTPFormulaFormat(PrintWriter output) {
		this.m_output = output;
	}

	public void printCNFFormula(Clause formula, TPTPFormulaRole role, String prefix) {
		Preconditions.checkArgument(prefix != null);
		m_output.print("cnf" + "(");
		switch(role) {
		case AXIOM:
			m_output.print(prefix + "axiom" + this.axiomCounter++ + "," + "axiom" + "," );
			m_output.print("(" + processClause(formula) + ")");
			break;
		case HYPOTHESIS:
			m_output.print(prefix + "hypothesis" + this.hypothesisCounter++ + "," + "hypothesis" + "," );
			m_output.print("(" + processClause(formula) + ")");
			break;
		case CONJECTURE:
			m_output.print(prefix + "negated_conjecture" + this.negatedConjectureCounter++ + "," + "negated_conjecture" + "," );
			m_output.print("(" + "~"+processClause(formula) + ")");
			break;
		default:
			throw new java.lang.RuntimeException("Unknown tptp formula role");	
		}
		m_output.print(") .");
	}

	public void printFOLFormula(Formula formula, TPTPFormulaRole role, String prefix) {
		Preconditions.checkArgument(prefix != null);
		m_output.print("fof" + "(");
		switch(role) {
		case AXIOM:
			m_output.print(prefix + "axiom" + this.axiomCounter++ + "," + "axiom" + "," );
			m_output.print(processFOLFormula(formula) );
			break;
		case HYPOTHESIS:
			m_output.print(prefix + "hypothesis" + this.hypothesisCounter++ + "," + "hypothesis" + "," );
			m_output.print(processFOLFormula(formula) );
			break;
		case CONJECTURE:
			m_output.print(prefix + "negated_conjecture" + this.negatedConjectureCounter++ + "," + "negated_conjecture" + "," );
			m_output.print("~"+processFOLFormula(formula) );
			break;
		default:
			throw new java.lang.RuntimeException("Unknown tptp formula role");	
		}
		m_output.print(") .");
	}

	private String processClause(Clause formula) {
		String outputString = "";
		int i = 0;
		for(Literal literal:((Clause)formula).getLiterals()) {
			outputString += processLiteral(literal);
			if(i < ((Clause)formula).getLiterals().size() - 1) {
				outputString += " | ";
			}
			++i;
		}
		return outputString;
	}

	private String processFOLFormula(Formula formula) {
		String outputString = "";
		if(formula instanceof QuantifiedFormula) {
			outputString += "(" + processQuantifiedVariables(((QuantifiedFormula)formula).getOperator(), ((QuantifiedFormula)formula).getBoundVariables()) + 
					processFOLFormula(((QuantifiedFormula)formula).getChildren().get(0)) + ")";
		}
		else if(formula instanceof Implication) {
			outputString += "(" + processFOLFormula(((Implication)formula).getChildren().get(0)) + " => " 
					+  processFOLFormula(((Implication)formula).getChildren().get(1)) + ")";
		}
		else if(formula instanceof Conjunction) {
			outputString += "(" + processFOLFormula(((Conjunction)formula).getChildren().get(0)) + " & " 
					+  processFOLFormula(((Conjunction)formula).getChildren().get(1)) + ")";
		}
		else if(formula instanceof Disjunction) {
			outputString += "(" + processFOLFormula(((Disjunction)formula).getChildren().get(0)) + " | " 
					+  processFOLFormula(((Disjunction)formula).getChildren().get(1)) + ")";
		}
		else if(formula instanceof Negation) {
			outputString += "(" + "~" + processFOLFormula(((Negation)formula).getChildren().get(0)) + ")";
		}
		else if(formula instanceof Atom) {
			outputString += processAtom((Atom)formula);
		}
		else if(formula instanceof Literal) {
			if(((Literal)formula).isPositive()) {
				outputString += processAtom((Atom)formula);
			}
			else {
				outputString += "~" + processAtom((Atom)formula);
			}
		}
		else {
			throw new java.lang.RuntimeException("Unknown formula type");
		}
		return outputString;
	}

	private String processLiteral(Literal literal) {
		String outputString = "";
		if(literal.isPositive()) {
			outputString += processAtom(literal);
		}
		else {
			outputString += "~" + processAtom(literal);
		}
		return outputString;
	}

	private String processAtom(Atom atom) {
		String outputString = atom.getPredicate().getName().toLowerCase();
		if(atom.getTerms().size() > 0) {
			outputString += "(";
			for(int i = 0; i < atom.getTerms().size(); ++ i) {
				outputString += processTerm(atom.getTerms().get(i));
				if(i < atom.getTerms().size() - 1) {
					outputString += ",";
				}
			}
			outputString += ")";
		}
		return outputString;
	}

	private String processTerm(Term term) {
		if(term instanceof Variable) {
			return processVariable((Variable)term);
		}
		else if(term instanceof Constant) {
			if(((Constant)term) instanceof UntypedConstant) {
				return "\"" + ((UntypedConstant)term).getSymbol().toUpperCase() + "\"" ;
			}
			else {
				return "\"" + ((TypedConstant<?>)term).getValue().toString().toUpperCase() + "\"" ;
			}
		}
		else if(term instanceof FunctionTerm) {
			String outputString = ((FunctionTerm)term).getFunction().getName().toLowerCase();
			outputString += "(";
			for(int i = 0; i < ((FunctionTerm)term).getTerms().size(); ++ i) {
				outputString += processTerm(((FunctionTerm)term).getTerms().get(i));
				if(i < ((FunctionTerm)term).getTerms().size() - 1) {
					outputString += ",";
				}
			}
			outputString += ")";
			return outputString;
		}
		throw new java.lang.RuntimeException("Unknown term type");
	}	

	private String processVariable(Variable variable) {
		return variable.getSymbol().toUpperCase();
	}	

	private String processQuantifiedVariables(LogicalSymbols operator, List<Variable> variables) {
		Preconditions.checkArgument(operator != null && (operator.equals(LogicalSymbols.UNIVERSAL) || operator.equals(LogicalSymbols.EXISTENTIAL)));
		Preconditions.checkArgument(variables != null && !variables.isEmpty());
		String outputString = "";
		outputString = operator.equals(LogicalSymbols.UNIVERSAL) ? "!" : "?";
		outputString += "[";
		for(int i = 0; i < variables.size(); ++i) {
			outputString += processVariable(variables.get(i));
			if(i < variables.size() - 1) {
				outputString += ",";
			}
		}
		outputString += "]:";
		return outputString;
	}	 

}
