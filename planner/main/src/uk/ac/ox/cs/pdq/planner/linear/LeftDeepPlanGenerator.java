package uk.ac.ox.cs.pdq.planner.linear;

import java.util.List;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.util.PlanCreationUtility;

/**
 * Transforms linear chase configurations to left-deep plans. 
 *
 * @author Efthymia Tsamoura
 */
public class LeftDeepPlanGenerator {

	/**
	 * Creates a linear plan using the subplans of the input sequence of nodes.
	 *
	 * @param <T> the generic type
	 * @param nodes List<T>
	 * @return the left deep plan
	 */
	public static<T extends SearchNode> RelationalTerm createLeftDeepPlan(List<T> nodes) {
		RelationalTerm parentPlan = null;
		for (T node: nodes) {
			RelationalTerm op1 = PlanCreationUtility.createSingleAccessPlan(node.getConfiguration().getRule().getBaseRelation(), node.getConfiguration().getRule().getAccessMethod(), node.getConfiguration().getFacts());
			if(parentPlan != null)
				parentPlan = PlanCreationUtility.createPlan(parentPlan,op1);
			else 
				parentPlan = op1;
		}
		return parentPlan;
	}
}
