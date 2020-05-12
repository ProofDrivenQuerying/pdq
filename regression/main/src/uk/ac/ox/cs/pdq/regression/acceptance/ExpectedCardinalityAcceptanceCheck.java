package uk.ac.ox.cs.pdq.regression.acceptance;

import static uk.ac.ox.cs.pdq.regression.acceptance.AcceptanceCriterion.AcceptanceLevels.FAIL;
import static uk.ac.ox.cs.pdq.regression.acceptance.AcceptanceCriterion.AcceptanceLevels.PASS;

import uk.ac.ox.cs.pdq.datasources.tuple.Table;

/**
 * Acceptance test requests the expected and observed results to be equivalent
 * under set semantics.
 * 
 * @author Julien Leblay
 */
public class ExpectedCardinalityAcceptanceCheck implements AcceptanceCriterion<Integer, Table> {

	/**
	 * 
	 *
	 * @param expected Integer
	 * @param observed Result
	 * @return AcceptanceResult
	 */
	@Override
	public AcceptanceResult check(Integer expected, Table observed) {
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
