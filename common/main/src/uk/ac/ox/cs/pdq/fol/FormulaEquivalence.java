package uk.ac.ox.cs.pdq.fol;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.LinkedHashMultiset;

/**
 * Approximate formula equivalence checking utility.
 * 
 * @author Julien Leblay
 */
public class FormulaEquivalence {

	/**
	 * Quickly attempts to determine if two formula are equivalence with one
	 * another. Assumes structures are homomorphic, and atom order match
	 * May return false negatives.
	 * @param left
	 * @param right
	 * @return boolean
	 */
	public static boolean approximateEquivalence(Formula left, Formula right) {
		if (!left.getClass().isAssignableFrom(right.getClass())
				&& !right.getClass().isAssignableFrom(left.getClass())) {
			return false;
		}
		if (left instanceof UnaryFormula) {
			return approximateUnaryEquivalence((UnaryFormula<?>) left, (UnaryFormula<?>) right) ;
		}
		if (left instanceof BinaryFormula) {
			return approximateBinaryEquivalence((BinaryFormula<?, ?>) left, (BinaryFormula<?, ?>) right) ;
		}
		if (left instanceof NaryFormula) {
			return approximateNaryEquivalence((NaryFormula<?>) left, (NaryFormula<?>) right) ;
		}
		if (left instanceof Predicate) {
			return approximateAtomEquivalence((Predicate) left, (Predicate) right);
		}
		return left == right;
	}

	/**
	 * @param left UnaryFormula<?>
	 * @param right UnaryFormula<?>
	 * @return boolean
	 */
	private static boolean approximateUnaryEquivalence(UnaryFormula<?> left, UnaryFormula<?> right) {
		if (!left.getSymbol().equals(right.getSymbol())) {
			return false;
		}
		return approximateEquivalence(left.getChild(), right.getChild());
	}

	/**
	 * @param left BinaryFormula<?,?>
	 * @param right BinaryFormula<?,?>
	 * @return boolean
	 */
	private static boolean approximateBinaryEquivalence(BinaryFormula<?, ?> left, BinaryFormula<?, ?> right) {
		if (!left.getSymbol().equals(right.getSymbol())) {
			return false;
		}
		if (!variableSignature(left.getPredicates()).equals(variableSignature(right.getPredicates()))) {
			return false;
		}
		return approximateEquivalence(left.getLeft(), right.getLeft())
				&& approximateEquivalence(left.getRight(), right.getRight());
	}

	/**
	 * @param left NaryFormula<?>
	 * @param right NaryFormula<?>
	 * @return boolean
	 */
	private static boolean approximateNaryEquivalence(NaryFormula<?> left, NaryFormula<?> right) {
		if (!left.getSymbol().equals(right.getSymbol())) {
			return false;
		}
		if (left.getPredicates().size() != right.getPredicates().size()) {
			return false;
		}
		LinkedHashMultiset<Signature> lSigs = LinkedHashMultiset.create();
		for (Predicate pred: left.getPredicates()) {
			lSigs.add(pred.getSignature());
		}
		LinkedHashMultiset<Signature> rSigs = LinkedHashMultiset.create();
		for (Predicate pred: right.getPredicates()) {
			rSigs.add(pred.getSignature());
		}
		if (!lSigs.equals(rSigs)) {
			return false;
		}
		if (!variableSignature(left.getPredicates()).equals(variableSignature(right.getPredicates()))) {
			return false;
		}
		Iterator<? extends Formula> li = left.getChildren().iterator();
		Iterator<? extends Formula> ri = right.getChildren().iterator();
		boolean result = true;
		for (int i = 0, l = left.size(); i < l; i++) {
			result &= approximateEquivalence(li.next(), ri.next());
		}
		return result;
	}

	/**
	 * @param atoms List<PredicateFormula>
	 * @return List<Integer>
	 */
	private static List<Integer> variableSignature(List<Predicate> atoms) {
		List<Integer> result = new LinkedList<>();
		Map<Term, Integer> varMap = new LinkedHashMap<>();
		int i = 0;
		for (Predicate atom: atoms) {
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
	 * @param left PredicateFormula
	 * @param right PredicateFormula
	 * @return boolean
	 */
	private static boolean approximateAtomEquivalence(Predicate left, Predicate right) {
		if (!left.getSignature().equals(right.getSignature())) {
			return false;
		}
		if (!left.getConstants().equals(right.getConstants())) {
			return false;
		}
		return true;
	}
}
