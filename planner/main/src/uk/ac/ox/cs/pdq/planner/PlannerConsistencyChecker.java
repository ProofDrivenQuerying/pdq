package uk.ac.ox.cs.pdq.planner;

import uk.ac.ox.cs.pdq.ConsistencyChecker;
import uk.ac.ox.cs.pdq.InconsistentParametersException;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;

// TODO: Auto-generated Javadoc
/**
 * Check high-level planner parameters consistency.
 *
 * @author Julien Leblay
 */
public class PlannerConsistencyChecker implements ConsistencyChecker<PlannerParameters, CostParameters, ReasoningParameters, DatabaseParameters> {

	/**
	 * Check.
	 *
	 * @param p PlannerParameters
	 * @param c CostParameters
	 * @param r the r
	 * @throws InconsistentParametersException the inconsistent parameters exception
	 * @see uk.ac.ox.cs.pdq.ConsistencyChecker#check(PlannerParameters)
	 */
	@Override
	public void check(PlannerParameters p, CostParameters c, ReasoningParameters r, DatabaseParameters d) throws InconsistentParametersException {
	}
	
//	/**
//	 * Checks if is left deep planner.
//	 *
//	 * @param p the p
//	 * @return true, if is left deep planner
//	 */
//	private boolean isLeftDeepPlanner(PlannerParameters p) {
//		switch(p.getPlannerType()) {
//		case DAG_GENERIC:
//		case DAG_SIMPLEDP:
//		case DAG_CHASEFRIENDLYDP:
//		case DAG_OPTIMIZED:
//			return false;
//		}
//		return true;
//	}
}
