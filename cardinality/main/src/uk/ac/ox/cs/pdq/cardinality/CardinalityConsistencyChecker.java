/*
 * 
 */
package uk.ac.ox.cs.pdq.cardinality;

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
public class CardinalityConsistencyChecker implements ConsistencyChecker<CardinalityParameters, CostParameters, ReasoningParameters> {

	/**
	 * Check.
	 *
	 * @param p PlannerParameters
	 * @param c CostParameters
	 * @param r the r
	 * @throws InconsistentParametersException the inconsistent parameters exception
	 * @see uk.ac.ox.cs.pdq.ConsistencyChecker#check(CardinalityParameters)
	 */
	@Override
	public void check(CardinalityParameters p, CostParameters c, ReasoningParameters r) throws InconsistentParametersException {
	}
}
