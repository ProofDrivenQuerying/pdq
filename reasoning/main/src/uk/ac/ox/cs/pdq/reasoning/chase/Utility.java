package uk.ac.ox.cs.pdq.reasoning.chase;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.fol.Atom;
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
import uk.ac.ox.cs.pdq.util.CanonicalNameGenerator;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

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
	public static Implication fire(Dependency dependency, Map<Variable, Constant> mapping, boolean skolemize) {
		Map<Variable, Constant> skolemizedMapping = mapping;
		if(skolemize) {
			skolemizedMapping = Utility.skolemizeMapping(dependency, mapping);
		}
		List<Formula> bodyAtoms = Lists.newArrayList();
		for(Atom atom:dependency.getBody().getAtoms()) {
			bodyAtoms.add(Utility.applySubstitution(atom, skolemizedMapping));
		}
		List<Formula> headAtoms = Lists.newArrayList();
		for(Atom atom:dependency.getHead().getAtoms()) {
			headAtoms.add(Utility.applySubstitution(atom, skolemizedMapping));
		}
		Formula bodyConjunction = Conjunction.of(bodyAtoms);
		Formula headConjunction = Conjunction.of(headAtoms);
		return Implication.of(bodyConjunction, headConjunction);
	}

	public static Implication fire(Dependency dependency, Map<Variable, Constant> mapping) {
		List<Formula> bodyAtoms = Lists.newArrayList();
		for(Atom atom:dependency.getBody().getAtoms()) {
			bodyAtoms.add(Utility.applySubstitution(atom, mapping));
		}
		List<Formula> headAtoms = Lists.newArrayList();
		for(Atom atom:dependency.getHead().getAtoms()) {
			headAtoms.add(Utility.applySubstitution(atom, mapping));
		}
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
		String namesOfUniversalVariables = "";
		Map<Variable, Constant> result = new LinkedHashMap<>(mapping);
		for (Variable variable: dependency.getUniversal()) {
			Variable variableTerm = variable;
			Preconditions.checkState(result.get(variableTerm) != null);
			namesOfUniversalVariables += variable.getSymbol() + result.get(variableTerm);
		}
		for(Variable variable:dependency.getExistential()) {
			if (!result.containsKey(variable)) {
				result.put(variable,
						new UntypedConstant(
								CanonicalNameGenerator.getName("TGD" + dependency.getId(),
										namesOfUniversalVariables,
										variable.getSymbol()))
						);
			}
		}
		return result;
	}

	public static Formula applySubstitution(Formula formula, Map<Variable, Constant> mapping) {
		if(formula instanceof Conjunction) {
			Formula child1 = applySubstitution(((Conjunction)formula).getChildren().get(0), mapping);
			Formula child2 = applySubstitution(((Conjunction)formula).getChildren().get(1), mapping);
			return Conjunction.of(child1, child2);
		}
		else if(formula instanceof Disjunction) {
			Formula child1 = applySubstitution(((Disjunction)formula).getChildren().get(0), mapping);
			Formula child2 = applySubstitution(((Disjunction)formula).getChildren().get(1), mapping);
			return Disjunction.of(child1, child2);
		}
		else if(formula instanceof Implication) {
			Formula child1 = applySubstitution(((Implication)formula).getChildren().get(0), mapping);
			Formula child2 = applySubstitution(((Implication)formula).getChildren().get(1), mapping);
			return Implication.of(child1, child2);
		}
		else if(formula instanceof ConjunctiveQuery) {
			List<Formula> bodyAtoms = new ArrayList<>();
			for (Atom atom: ((ConjunctiveQuery)formula).getAtoms()) {
				bodyAtoms.add(applySubstitution(atom,mapping));
			}
			return Conjunction.of(bodyAtoms);
		}
		else if(formula instanceof Atom) {
			List<Term> nterms = new ArrayList<>();
			for (Term term: ((Atom)formula).getTerms()) {
				if (term.isVariable() && mapping.containsKey(term)) {
					nterms.add(mapping.get(term));
				} else {
					nterms.add(term);
				}
			}
			return new Atom(((Atom)formula).getPredicate(), nterms);
		}
		throw new java.lang.RuntimeException("Unsupported formula type");
	}

}
