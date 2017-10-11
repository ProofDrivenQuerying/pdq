package uk.ac.ox.cs.pdq.planner.events;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.util.EventHandler;

import com.google.common.eventbus.Subscribe;

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
	public void printPlan(RelationalTerm plan, Cost cost) {
		if (plan != null && cost != null && cost.getValue().doubleValue() != Double.POSITIVE_INFINITY) 
			System.out.println(plan.toString());
	}

}
