package formats;

import java.io.PrintWriter;

import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.FunctionTerm;
import uk.ac.ox.cs.pdq.fol.Implication;
import uk.ac.ox.cs.pdq.fol.Literal;
import uk.ac.ox.cs.pdq.fol.Negation;
import uk.ac.ox.cs.pdq.fol.QuantifiedFormula;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;

public class DLVFormulaFormat {

	protected final PrintWriter m_output;

	public DLVFormulaFormat(PrintWriter output) {
		this.m_output = output;
	}

	public void printFOLFormula(Dependency formula) {
		m_output.print(processFOLFormula(formula) );
		m_output.print(" .");
	}

	public void printFOLFormula(Atom formula) {
		m_output.print(processFOLFormula(formula) );
		m_output.print(" .");
	}

	public void printNegatedQuery(ConjunctiveQuery query) {
		m_output.print(":- ");
		m_output.print(processFOLFormula(query) );
		m_output.print(" .");
	}

	private String processFOLFormula(Formula formula) {
		String outputString = "";
		if(formula instanceof QuantifiedFormula) {
			outputString +=  
					processFOLFormula(((QuantifiedFormula)formula).getChildren().get(0)) ;
		}
		else if(formula instanceof Implication) {
			outputString += processFOLFormula(((Implication)formula).getChildren().get(1)) + ":-" + 
					processFOLFormula(((Implication)formula).getChildren().get(0));
		}
		else if(formula instanceof Conjunction) {
			outputString += processFOLFormula(((Conjunction)formula).getChildren().get(0)) + ", " 
					+  processFOLFormula(((Conjunction)formula).getChildren().get(1));
		}
		else if(formula instanceof Disjunction) {
			outputString += processFOLFormula(((Disjunction)formula).getChildren().get(0)) + " v " 
					+  processFOLFormula(((Disjunction)formula).getChildren().get(1));
		}
		else if(formula instanceof Negation) {
			throw new java.lang.RuntimeException("Negation is not currently supported");
		}
		else if(formula instanceof Atom) {
			outputString += processAtom((Atom)formula);
		}
		else if(formula instanceof Literal) {
			outputString += processLiteral((Literal)formula);
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
			outputString += "not " + processAtom(literal);
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

}
