package uk.ac.ox.cs.pdq.planner.reasoning;

import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating Match objects.
 *
 * @author Efthymia Tsamoura
 */
public class MatchFactory {

	/**
	 * Gets the match.
	 *
	 * @param axiom the axiom
	 * @param fact the fact
	 * @return 		a match given the input accessibility axiom and the input fact
	 */
	public static Match getMatch(AccessibilityAxiom axiom, Atom fact) {
		Map<Variable, Constant> map = createMapping(axiom, fact);
		return new Match(axiom, map);
	}

	/**
	 * Creates a new Match object.
	 *
	 * @param axiom AccessibilityAxiom
	 * @param fact PredicateFormula
	 * @return Map<Variable, Constant>
	 */
	private static Map<Variable, Constant> createMapping(AccessibilityAxiom axiom, Atom fact) {
		Map<Variable, Constant> map = new LinkedHashMap<>();
		int i = 0;
		for(Term term:axiom.getGuard().getTerms()) {
			if(term instanceof Variable && fact.getTerm(i) instanceof Constant) {
				map.put((Variable)term, (Constant) fact.getTerm(i));
			}
			else {
				throw new java.lang.RuntimeException("Cannot map constants to constants or variables to variables, but only variables to constants");
			}
			i++;
		}
		return map;
	}
}
