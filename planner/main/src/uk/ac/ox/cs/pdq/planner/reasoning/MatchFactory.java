package uk.ac.ox.cs.pdq.planner.reasoning;

import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.accessible.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.reasoning.Match;
/**
 * Creates Match objects
 *
 * @author Efthymia Tsamoura
 */
public class MatchFactory {

	/**
	 *
	 * @param axiom
	 * @param fact
	 * @return
	 * 		a match given the input accessibility axiom and the input fact
	 */
	public static Match getMatch(AccessibilityAxiom axiom, Predicate fact) {
		Map<Variable, Constant> map = createMap(axiom, fact);
//		if(fact instanceof BagBoundPredicate) {
//			return new BagMatch(axiom, map, ((BagBoundPredicate) fact).getBag());
//		}
		return new Match(axiom, map);
	}

	/**
	 * @param axiom AccessibilityAxiom
	 * @param fact PredicateFormula
	 * @return Map<Variable, Constant>
	 */
	private static Map<Variable, Constant> createMap(AccessibilityAxiom axiom, Predicate fact) {
		Map<Variable, Constant> map = new LinkedHashMap<>();
		int i = 0;
		for(Variable variable:axiom.getGuard().getVariables()) {
			map.put(variable, (Constant) fact.getTerm(i));
			i++;
		}
		return map;
	}
}
