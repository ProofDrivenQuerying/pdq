package uk.ac.ox.cs.pdq.planner;

import uk.ac.ox.cs.pdq.ConsistencyChecker;
import uk.ac.ox.cs.pdq.InconsistentParametersException;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;

/**
 * Check high-level planner parameters consistency.
 *
 * @author Julien Leblay
 */
public class PlannerConsistencyChecker implements ConsistencyChecker<PlannerParameters, CostParameters, ReasoningParameters> {

	/**
	 * @param p PlannerParameters
	 * @param c CostParameters
	 * @throws InconsistentParametersException
	 * @see uk.ac.ox.cs.pdq.ConsistencyChecker#check(PlannerParameters)
	 */
	@Override
	public void check(PlannerParameters p, CostParameters c, ReasoningParameters r) throws InconsistentParametersException {
	}
	
	private boolean isLeftDeepPlanner(PlannerParameters p) {
		switch(p.getPlannerType()) {
		case DAG_GENERIC:
		case DAG_SIMPLEDP:
		case DAG_CHASEFRIENDLYDP:
		case DAG_OPTIMIZED:
			return false;
		}
		return true;
	}
}
