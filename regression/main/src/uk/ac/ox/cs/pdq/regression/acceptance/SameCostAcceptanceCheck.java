// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.regression.acceptance;

import static uk.ac.ox.cs.pdq.regression.acceptance.AcceptanceCriterion.AcceptanceLevels.FAIL;
import static uk.ac.ox.cs.pdq.regression.acceptance.AcceptanceCriterion.AcceptanceLevels.PASS;

import java.util.Map.Entry;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.regression.utils.PlannerTestUtilities;

/**
 * Acceptance test requests that the expected and observed plans  have the same cost in order
 * to pass.
 * 
 * @author Julien Leblay
 */
public class SameCostAcceptanceCheck implements AcceptanceCriterion<Entry<RelationalTerm, Cost>, Entry<RelationalTerm, Cost>> {

	/**
	 * 
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
			return new AcceptanceResult(FAIL,
					"Plan found while expected none");
		}
		switch (PlannerTestUtilities.howDifferent(expectedPlan.getKey(), expectedPlan.getValue(), observedPlan.getKey(), observedPlan.getValue())) {
		case IDENTICAL:
			return new AcceptanceResult(PASS, "Perfect match.");
		case EQUIVALENT:
			return new AcceptanceResult(PASS,
					"Plans differ, but have same costs. - ",
					"diff: " + PlannerTestUtilities.diff(expectedPlan.getKey(), expectedPlan.getValue(), observedPlan.getKey(), observedPlan.getValue()));
		default:
			return new AcceptanceResult(FAIL,
					"expected: " + expectedPlan.getValue() + " - " + expectedPlan.getKey().getAccesses(),
					"observed: " + (observedPlan != null ?  observedPlan.getValue() + " - " + observedPlan.getKey().getAccesses(): "<no plan>"));
		}
	}

}
