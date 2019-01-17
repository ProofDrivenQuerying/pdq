package uk.ac.ox.cs.pdq.fol;

import java.io.Serializable;
import java.util.Map;

/**
 * Top-level FO formula.
 *
 * @author Efthymia Tsamoura
 */
public abstract class Formula implements Serializable{
	private static final long serialVersionUID = -398980058943314856L;

	public abstract int getId();
	
	public abstract <T extends Formula> T[] getChildren();
	
	public abstract Formula getChild(int childIndex);
	
	public abstract int getNumberOfChildren();

	public abstract Atom[] getAtoms();

	public abstract Term[] getTerms();
	
	public abstract Variable[] getFreeVariables();
	
	public abstract Variable[] getBoundVariables();
	
	
	/** 
	 * Creates a new Formula using the given variable to constant map and substitutes all variables in this new formula. 
	 * This function will not change the this formula object.
	 * @param substitution
	 * @return the new formula with the substitutions.
	 */
	public Formula applySubstitution(Map<Variable, Constant> substitution) {
		return Formula.applySubstitution(this, substitution);
	}

	/** 
	 * Same as above but it is a statis function.
	 * @param formula
	 * @param substitution
	 * @return
	 */
	public static Formula applySubstitution(Formula formula, Map<Variable, Constant> substitution) {
		if(formula instanceof Conjunction) {
			Formula child1 = applySubstitution(((Conjunction)formula).getChildren()[0], substitution);
			Formula child2 = applySubstitution(((Conjunction)formula).getChildren()[1], substitution);
			return Conjunction.create(child1, child2);
		}
		else if(formula instanceof Disjunction) {
			Formula child1 = applySubstitution(((Disjunction)formula).getChildren()[0], substitution);
			Formula child2 = applySubstitution(((Disjunction)formula).getChildren()[1], substitution);
			return Disjunction.of(child1, child2);
		}
		else if(formula instanceof Implication) {
			Formula child1 = applySubstitution(((Implication)formula).getChildren()[0], substitution);
			Formula child2 = applySubstitution(((Implication)formula).getChildren()[1], substitution);
			return Implication.of(child1, child2);
		}
		else if(formula instanceof ConjunctiveQuery) {
			Atom[] atoms = ((ConjunctiveQuery)formula).getAtoms();
			Formula[] bodyAtoms = new Formula[atoms.length];
			for (int atomIndex = 0; atomIndex < atoms.length; ++atomIndex) 
				bodyAtoms[atomIndex] = applySubstitution(atoms[atomIndex],substitution);
			return Conjunction.create(bodyAtoms);
		}
		else if(formula instanceof Atom) {
			Term[] nterms = new Term[((Atom)formula).getNumberOfTerms()];
			for (int termIndex = 0; termIndex < ((Atom)formula).getNumberOfTerms(); ++termIndex) {
				Term term = ((Atom)formula).getTerm(termIndex);
				if (term.isVariable() && substitution.containsKey(term)) 
					nterms[termIndex] = substitution.get(term);
				else 
					nterms[termIndex] = term;
			}
			return Atom.create(((Atom)formula).getPredicate(), nterms);
		}
		throw new java.lang.RuntimeException("Unsupported formula type");
	}
}
