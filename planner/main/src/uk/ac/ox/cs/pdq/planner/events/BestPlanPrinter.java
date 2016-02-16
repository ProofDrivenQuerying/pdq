package uk.ac.ox.cs.pdq.planner.events;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.io.pretty.PrettyDAGPlanWriter;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.plan.Plan;

import com.google.common.eventbus.Subscribe;

// TODO: Auto-generated Javadoc
/**
 * The Class BestPlanPrinter.
 */
public class BestPlanPrinter implements EventHandler {
	
	/**
	 * Prints the plan.
	 *
	 * @param plan the plan
	 */
	@Subscribe
	public void printPlan(Plan plan) {
		if (plan != null && plan.getCost() != null && plan.getCost().getValue().doubleValue() != Double.POSITIVE_INFINITY) {
//			Writers.xml().write(plan);
			PrettyDAGPlanWriter.to(System.out).write((DAGPlan) plan);
		}
	}

}
