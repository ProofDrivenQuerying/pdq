package uk.ac.ox.cs.pdq.test.acceptance;

import static uk.ac.ox.cs.pdq.test.acceptance.AcceptanceCriterion.AcceptanceLevels.FAIL;
import static uk.ac.ox.cs.pdq.test.acceptance.AcceptanceCriterion.AcceptanceLevels.PASS;

import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.plan.Plan;

// TODO: Auto-generated Javadoc
/**
 * Acceptance test request the expected plan cost to be within an order of 
 * magnitude of the expected plan cost to pass.
 * 
 * @author Julien Leblay
 */
public class ApproximateCostAcceptanceCheck implements AcceptanceCriterion<Plan, Plan> {

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
		Cost expectedCost = expectedPlan.getCost();
		if (observedPlan != null) {
			Cost observedCost = observedPlan.getCost();
			if (expectedCost instanceof DoubleCost
					&& observedCost instanceof DoubleCost) {
				double ec = expectedCost.getValue().doubleValue();
				double oc = observedCost.getValue().doubleValue();
				if (ec * .1 <= oc && oc <= ec * 10.) {
					return new AcceptanceResult(PASS,
						"expected: " + expectedPlan.getCost() + " - " + expectedPlan.getAccesses(),
						"observed: " + observedPlan.getCost() + " - " + observedPlan.getAccesses());
				}
			}
		}
		return new AcceptanceResult(FAIL,
				"expected: " + expectedPlan.getCost() + " - " + expectedPlan.getAccesses(),
				"observed: " + (observedPlan != null ?  observedPlan.getCost() + " - " + observedPlan.getAccesses(): null));
	}

}
