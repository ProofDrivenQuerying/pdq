package uk.ac.ox.cs.pdq.test.acceptance;

import static uk.ac.ox.cs.pdq.test.acceptance.AcceptanceCriterion.AcceptanceLevels.FAIL;
import static uk.ac.ox.cs.pdq.test.acceptance.AcceptanceCriterion.AcceptanceLevels.PASS;

import java.util.Map.Entry;

import uk.ac.ox.cs.pdq.algebra.AlgebraUtilities;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.DoubleCost;

// TODO: Auto-generated Javadoc
/**
 * Acceptance test request the expected plan cost to be within an order of 
 * magnitude of the expected plan cost to pass.
 * 
 * @author Julien Leblay
 */
public class ApproximateCostAcceptanceCheck implements AcceptanceCriterion<Entry<RelationalTerm, Cost>, Entry<RelationalTerm, Cost>> {

	/**
	 * Check.
	 *
	 * @param expectedPlan Plan
	 * @param observedPlan Plan
	 * @return AcceptanceResult
	 */
	@Override
	public AcceptanceResult check(Entry<RelationalTerm, Cost> expectedPlan, Entry<RelationalTerm, Cost> observedPlan) {
		if (expectedPlan == null) {
			if (observedPlan == null) {
				return new AcceptanceResult(PASS, "No plan found");
			}
			return new AcceptanceResult(FAIL, "Plan found while expected none");
		}
		Cost expectedCost = expectedPlan.getValue();
		if (observedPlan != null) {
			Cost observedCost = observedPlan.getValue();
			if (expectedCost instanceof DoubleCost
					&& observedCost instanceof DoubleCost) {
				double ec = expectedCost.getValue().doubleValue();
				double oc = observedCost.getValue().doubleValue();
				if (ec * .1 <= oc && oc <= ec * 10.) {
					return new AcceptanceResult(PASS,
						"expected: " + expectedPlan.getValue() + " - " + AlgebraUtilities.getAccesses(expectedPlan.getKey()),
						"observed: " + observedPlan.getValue() + " - " + AlgebraUtilities.getAccesses(observedPlan.getKey()));
				}
			}
		}
		return new AcceptanceResult(FAIL,
				"expected: " + expectedPlan.getValue() + " - " + AlgebraUtilities.getAccesses(expectedPlan.getKey()),
				"observed: " + (observedPlan != null ?  observedPlan.getValue() + " - " + AlgebraUtilities.getAccesses(observedPlan.getKey()): null));
	}

}
