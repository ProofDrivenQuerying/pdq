package uk.ac.ox.cs.pdq.test.planner;

import uk.ac.ox.cs.pdq.algebra.AlgebraUtilities;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;

public class PlannerTestUtilities {

	/**
	 * The Enum Levels.
	 */
	public static enum Levels {
		/** The identical. */
		IDENTICAL, 
		/** The equivalent. */
		EQUIVALENT, 
		/** The different. */
		DIFFERENT}

	/**
	 * TOCOMMENT: WHAT IS THIS?.
	 *
	 * @param o Plan
	 * @return Levels
	 */
	public static Levels howDifferent(RelationalTerm s, Cost sCost, RelationalTerm o, Cost oCost) {
		if (o == null) {
			return Levels.DIFFERENT;
		}
		if (sCost.equals(oCost)) {
			if (s.equals(o)) {
				return Levels.IDENTICAL;
			}
			return Levels.EQUIVALENT;
		}
		return Levels.DIFFERENT;
	}

	/**
	 * Diff.
	 *
	 * @param o Plan
	 * @return String
	 */
	public static String diff(RelationalTerm s, Cost sCost, RelationalTerm o, Cost oCost) {
		StringBuilder result = new StringBuilder();
		result.append("\n\tCosts: ").append(sCost).append(" <-> ").append(oCost);
		result.append("\n\tLeaves:\n\t\t");
		result.append(AlgebraUtilities.getAccesses(s)).append("\n\t\t");
		result.append(AlgebraUtilities.getAccesses(o)).append("\n\t");
		return result.toString();
	}

}
