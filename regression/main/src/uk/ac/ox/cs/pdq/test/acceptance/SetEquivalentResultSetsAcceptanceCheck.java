package uk.ac.ox.cs.pdq.test.acceptance;

import static uk.ac.ox.cs.pdq.test.acceptance.AcceptanceCriterion.AcceptanceLevels.FAIL;
import static uk.ac.ox.cs.pdq.test.acceptance.AcceptanceCriterion.AcceptanceLevels.PASS;

import uk.ac.ox.cs.pdq.datasources.utility.Result;

/**
 * Acceptance test request the expected and observed results to be equivalent
 * under set semantics.
 * 
 * @author Julien Leblay
 */
public class SetEquivalentResultSetsAcceptanceCheck implements AcceptanceCriterion<Result, Result> {

	/**
	 * Check.
	 *
	 * @param expected Result
	 * @param observed Result
	 * @return AcceptanceResult
	 */
	@Override
	public AcceptanceResult check(Result expected, Result observed) {
		if (expected == null) {
			if (observed == null || observed.isEmpty()) {
				return new AcceptanceResult(PASS, "No result sets found");
			}
			return new AcceptanceResult(FAIL,
					"Result set found while expected none");
		}
		switch (expected.howDifferent(observed)) {
		case IDENTICAL:
			return new AcceptanceResult(PASS, "Perfect match.");
		case EQUIVALENT:
			return new AcceptanceResult(PASS,
					"Equivalent result sets (" + expected.size() + " vs. " + expected.size() + " results)");
		default:
			return new AcceptanceResult(FAIL,
					"diff - " + observed.diff(expected));
		}
	}

}
