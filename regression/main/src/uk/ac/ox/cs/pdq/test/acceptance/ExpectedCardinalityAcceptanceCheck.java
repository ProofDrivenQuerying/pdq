package uk.ac.ox.cs.pdq.test.acceptance;

import static uk.ac.ox.cs.pdq.test.acceptance.AcceptanceCriterion.AcceptanceLevels.FAIL;
import static uk.ac.ox.cs.pdq.test.acceptance.AcceptanceCriterion.AcceptanceLevels.PASS;
import uk.ac.ox.cs.pdq.util.Result;

/**
 * Acceptance test request the expected and observed results to be equivalent
 * under set semantics.
 * 
 * @author Julien Leblay
 */
public class ExpectedCardinalityAcceptanceCheck implements AcceptanceCriterion<Integer, Result> {

	/**
	 * @param expected Integer
	 * @param observed Result
	 * @return AcceptanceResult
	 */
	@Override
	public AcceptanceResult check(Integer expected, Result observed) {
		if (observed == null || observed.isEmpty()) {
				return new AcceptanceResult(PASS, "No result sets found");
		}
		if (expected == observed.size()) {
			return new AcceptanceResult(PASS, "Found " + expected + " results as expected");
		}
		return new AcceptanceResult(FAIL,
				"Expected result cardinality to be " + expected + " found " + observed.size());
	}

}
