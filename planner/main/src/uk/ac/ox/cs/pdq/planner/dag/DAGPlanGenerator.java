package uk.ac.ox.cs.pdq.planner.dag;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.planner.util.PlanCreationUtility;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class DAGPlanGenerator {
		
	public static RelationalTerm toPlan(ApplyRule config) {
		return PlanCreationUtility.createSingleAccessPlan(((ApplyRule) config).getRelation(), ((ApplyRule) config).getRule().getAccessMethod(), ((ApplyRule) config).getFacts());
	}
}
