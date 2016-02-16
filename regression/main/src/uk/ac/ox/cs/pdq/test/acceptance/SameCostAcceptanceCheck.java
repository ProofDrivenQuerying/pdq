package uk.ac.ox.cs.pdq.test.acceptance;

import static uk.ac.ox.cs.pdq.test.acceptance.AcceptanceCriterion.AcceptanceLevels.FAIL;
import static uk.ac.ox.cs.pdq.test.acceptance.AcceptanceCriterion.AcceptanceLevels.PASS;
import uk.ac.ox.cs.pdq.plan.Plan;

// TODO: Auto-generated Javadoc
/**
 * Acceptance test request the expected and observed plans to have the same cost
 * to pass.
 * 
 * @author Julien Leblay
 */
public class SameCostAcceptanceCheck implements AcceptanceCriterion<Plan, Plan> {

	/**
	 * Check.
	 *
	 * @param expectedPlan Plan
	 * @param observedPlan Plan
	 * @return AcceptanceResult
	 */
	@Override
	public AcceptanceResult check(Plan expectedPlan, Plan observedPlan) {
		if (expectedPlan == null) {
			if (observedPlan == null || observedPlan.isEmpty()) {
				return new AcceptanceResult(PASS, "No plan found");
			}
			return new AcceptanceResult(FAIL,
					"Plan found while expected none");
		}
		switch (expectedPlan.howDifferent(observedPlan)) {
		case IDENTICAL:
			return new AcceptanceResult(PASS, "Perfect match.");
		case EQUIVALENT:
			return new AcceptanceResult(PASS,
					"Plans differ, but have same costs. - ",
					"diff: " + expectedPlan.diff(observedPlan));
		default:
			return new AcceptanceResult(FAIL,
					"expected: " + expectedPlan.getCost() + " - " + expectedPlan.getAccesses(),
					"observed: " + (observedPlan != null ?  observedPlan.getCost() + " - " + observedPlan.getAccesses(): "<no plan>"));
		}
	}

}
