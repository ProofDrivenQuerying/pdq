package uk.ac.ox.cs.pdq.reasoning.chase;

import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.CanonicalNameGenerator;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Implication;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class Utility {

	/**
	 * Fire.
	 *
	 * @param mapping Map<Variable,Term>
	 * @param skolemize boolean
	 * @return TGD<L,R>
	 * @see uk.ac.ox.cs.pdq.ics.IC#fire(Map<Variable,Term>, boolean)
	 */
	public static Implication ground(Dependency dependency, Map<Variable, Constant> mapping, boolean skolemize) {
		Map<Variable, Constant> skolemizedMapping = mapping;
		if(skolemize) 
			skolemizedMapping = Utility.skolemizeMapping(dependency, mapping);
		Formula[] bodyAtoms = new Formula[dependency.getNumberOfBodyAtoms()];
		for(int bodyAtomIndex = 0; bodyAtomIndex < dependency.getNumberOfBodyAtoms(); ++bodyAtomIndex) 
			bodyAtoms[bodyAtomIndex] = Utility.applySubstitution(dependency.getBodyAtom(bodyAtomIndex), skolemizedMapping);

		Formula[] headAtoms = new Formula[dependency.getNumberOfHeadAtoms()];
		for(int headAtomIndex = 0; headAtomIndex < dependency.getNumberOfHeadAtoms(); ++headAtomIndex) 
			headAtoms[headAtomIndex] = Utility.applySubstitution(dependency.getHeadAtom(headAtomIndex), skolemizedMapping);
		Formula bodyConjunction = Conjunction.of(bodyAtoms);
		Formula headConjunction = Conjunction.of(headAtoms);
		return Implication.of(bodyConjunction, headConjunction);
	}

	public static Implication fire(Dependency dependency, Map<Variable, Constant> mapping) {
		Formula[] bodyAtoms = new Formula[dependency.getNumberOfBodyAtoms()];
		for(int bodyAtomIndex = 0; bodyAtomIndex < dependency.getNumberOfBodyAtoms(); ++bodyAtomIndex) 
			bodyAtoms[bodyAtomIndex] = Utility.applySubstitution(dependency.getBodyAtom(bodyAtomIndex), mapping);

		Formula[] headAtoms = new Formula[dependency.getNumberOfHeadAtoms()];
		for(int headAtomIndex = 0; headAtomIndex < dependency.getNumberOfHeadAtoms(); ++headAtomIndex) 
			headAtoms[headAtomIndex] = Utility.applySubstitution(dependency.getHeadAtom(headAtomIndex), mapping);
		Formula bodyConjunction = Conjunction.of(bodyAtoms);
		Formula headConjunction = Conjunction.of(headAtoms);
		return Implication.of(bodyConjunction, headConjunction);
	}

	/**
	 * TOCOMMENT there is no "canonicalNames" mentioned in the comment says here.
	 * Skolemize mapping.
	 *
	 * @param mapping the mapping
	 * @return 		If canonicalNames is TRUE returns a copy of the input mapping
	 * 		augmented such that Skolem constants are produced for
	 *      the existentially quantified variables
	 */
	public static Map<Variable, Constant> skolemizeMapping(Dependency dependency, Map<Variable, Constant> mapping) {
		Map<Variable, Constant> result = new LinkedHashMap<>(mapping);
		for(Variable variable:dependency.getExistential()) {
			if (!result.containsKey(variable)) {
				result.put(variable, 
						UntypedConstant.create(CanonicalNameGenerator.getTriggerWitness(dependency, mapping, variable)));
			}
		}
		
		return result;
	}

	public static Formula applySubstitution(Formula formula, Map<Variable, Constant> mapping) {
		if(formula instanceof Conjunction) {
			Formula child1 = applySubstitution(((Conjunction)formula).getChildren()[0], mapping);
			Formula child2 = applySubstitution(((Conjunction)formula).getChildren()[1], mapping);
			return Conjunction.of(child1, child2);
		}
		else if(formula instanceof Disjunction) {
			Formula child1 = applySubstitution(((Disjunction)formula).getChildren()[0], mapping);
			Formula child2 = applySubstitution(((Disjunction)formula).getChildren()[1], mapping);
			return Disjunction.of(child1, child2);
		}
		else if(formula instanceof Implication) {
			Formula child1 = applySubstitution(((Implication)formula).getChildren()[0], mapping);
			Formula child2 = applySubstitution(((Implication)formula).getChildren()[1], mapping);
			return Implication.of(child1, child2);
		}
		else if(formula instanceof ConjunctiveQuery) {
			Atom[] atoms = ((ConjunctiveQuery)formula).getAtoms();
			Formula[] bodyAtoms = new Formula[atoms.length];
			for (int atomIndex = 0; atomIndex < atoms.length; ++atomIndex) 
				bodyAtoms[atomIndex] = applySubstitution(atoms[atomIndex],mapping);
			return Conjunction.of(bodyAtoms);
		}
		else if(formula instanceof Atom) {
			Term[] nterms = new Term[((Atom)formula).getNumberOfTerms()];
			for (int termIndex = 0; termIndex < ((Atom)formula).getNumberOfTerms(); ++termIndex) {
				Term term = ((Atom)formula).getTerm(termIndex);
				if (term.isVariable() && mapping.containsKey(term)) 
					nterms[termIndex] = mapping.get(term);
				else 
					nterms[termIndex] = term;
			}
			return Atom.create(((Atom)formula).getPredicate(), nterms);
		}
		throw new java.lang.RuntimeException("Unsupported formula type");
	}

}
