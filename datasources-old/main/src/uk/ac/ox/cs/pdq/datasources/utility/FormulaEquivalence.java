package uk.ac.ox.cs.pdq.datasources.utility;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Disjunction;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Implication;
import uk.ac.ox.cs.pdq.fol.Negation;
import uk.ac.ox.cs.pdq.fol.QuantifiedFormula;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * Approximate formula equivalence checking utility.
 * TOCOMMENT: WHY IS THIS HERE IN THIS PACKAGED?
 * 
 * @author Julien Leblay
 */
public class FormulaEquivalence {

	/**
	 * Quickly attempts to determine if two formulas are equivalent to one
	 * another. 
	 * Returns true in case the predicates are the same and the same constants and variables.
	 * 
	 * May return false negatives.
	 *
	 * @param left the left
	 * @param right the right
	 * @return boolean
	 */
	public static boolean approximateEquivalence(Formula left, Formula right) {
		if (!left.getClass().isAssignableFrom(right.getClass())
				&& !right.getClass().isAssignableFrom(left.getClass())) {
			return false;
		}
		if (left instanceof Negation) {
			return approximateUnaryEquivalence((Negation) left, (Negation) right) ;
		}
		if (left instanceof Conjunction) {
			return approximateBinaryEquivalence((Conjunction) left, (Conjunction) right) ;
		}
		if (left instanceof Disjunction) {
			return approximateBinaryEquivalence((Disjunction) left, (Disjunction) right) ;
		}
		if (left instanceof Implication) {
			return approximateBinaryEquivalence((Implication) left, (Implication) right) ;
		}
		if (left instanceof Dependency) {
			return approximateBinaryEquivalence((Dependency) left, (Dependency) right) ;
		}
		if (left instanceof QuantifiedFormula) {
			return approximateUnaryEquivalence((QuantifiedFormula) left, (QuantifiedFormula) right) ;
		}
		if (left instanceof Atom) {
			return approximateAtomEquivalence((Atom) left, (Atom) right);
		}
		return left == right;
	}

	/**
	 *
	 * @param left UnaryFormula<?>
	 * @param right UnaryFormula<?>
	 * @return boolean
	 */
	private static boolean approximateUnaryEquivalence(Negation left, Negation right) {
		return approximateEquivalence(left.getChildren()[0], right.getChildren()[0]);
	}

	/**
	 *
	 * @param left BinaryFormula<?,?>
	 * @param right BinaryFormula<?,?>
	 * @return boolean
	 */
	private static boolean approximateBinaryEquivalence(Conjunction left, Conjunction right) {
		if (!variableSignature(left.getAtoms()).equals(variableSignature(right.getAtoms()))) {
			return false;
		}
		return approximateEquivalence(left.getChildren()[0], right.getChildren()[0])
				&& approximateEquivalence(left.getChildren()[1], right.getChildren()[1]);
	}
	
	private static boolean approximateBinaryEquivalence(Disjunction left, Disjunction right) {
		if (!variableSignature(left.getAtoms()).equals(variableSignature(right.getAtoms()))) {
			return false;
		}
		return approximateEquivalence(left.getChildren()[0], right.getChildren()[0])
				&& approximateEquivalence(left.getChildren()[1], right.getChildren()[1]);
	}
	
	private static boolean approximateBinaryEquivalence(Implication left, Implication right) {
		if (!variableSignature(left.getAtoms()).equals(variableSignature(right.getAtoms()))) {
			return false;
		}
		return approximateEquivalence(left.getChildren()[0], right.getChildren()[0])
				&& approximateEquivalence(left.getChildren()[1], right.getChildren()[1]);
	}
	
	private static boolean approximateBinaryEquivalence(Dependency left, Dependency right) {
		if (!variableSignature(left.getAtoms()).equals(variableSignature(right.getAtoms()))) {
			return false;
		}
		return approximateEquivalence(left.getChildren()[0], right.getChildren()[0]);
	}
	
	private static boolean approximateUnaryEquivalence(QuantifiedFormula left, QuantifiedFormula right) {
		if (!variableSignature(left.getAtoms()).equals(variableSignature(right.getAtoms()))) {
			return false;
		}
		if(!left.getTopLevelQuantifiedVariables().equals(right.getTopLevelQuantifiedVariables())) {
			return false;
		}
		return approximateEquivalence(left.getChildren()[0], right.getChildren()[0]);
	}
	
	/**
	 *
	 * @param atoms List<PredicateFormula>
	 * @return List<Integer>
	 */
	private static List<Integer> variableSignature(Atom[] atoms) {
		List<Integer> result = new LinkedList<>();
		Map<Term, Integer> varMap = new LinkedHashMap<>();
		int i = 0;
		for (Atom atom: atoms) {
			for (Term term: atom.getTerms()) {
				Integer j = varMap.get(term);
				if (j == null) {
					varMap.put(term, (j = i++));
				}
				result.add(j);
			}
		}
		return result;
	}

	/**
	 *
	 * @param left PredicateFormula
	 * @param right PredicateFormula
	 * @return boolean
	 */
	private static boolean approximateAtomEquivalence(Atom left, Atom right) {
		if (!left.getPredicate().equals(right.getPredicate())) {
			return false;
		}
		if (!Utility.getTypedAndUntypedConstants(left).equals(Utility.getTypedAndUntypedConstants(right))) {
			return false;
		}
		return true;
	}
}
