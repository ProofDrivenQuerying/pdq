/*
 * 
 */
package uk.ac.ox.cs.pdq.planner;

import uk.ac.ox.cs.pdq.ConsistencyChecker;
import uk.ac.ox.cs.pdq.InconsistentParametersException;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;

// TODO: Auto-generated Javadoc
/**
 * Check high-level planner parameters consistency.
 *
 * @author Julien Leblay
 */
public class PlannerConsistencyChecker implements ConsistencyChecker<PlannerParameters, CostParameters, ReasoningParameters> {

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
	public void check(PlannerParameters p, CostParameters c, ReasoningParameters r) throws InconsistentParametersException {
	}
}
