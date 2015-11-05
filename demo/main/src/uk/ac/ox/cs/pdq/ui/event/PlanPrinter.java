package uk.ac.ox.cs.pdq.ui.event;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.io.pretty.PrettyLinearPlanWriter;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;

import com.google.common.eventbus.Subscribe;

/**
 * Statistics logger to high-level explorer informations.
 * 
 * @author Julien Leblay
 */
public class PlanPrinter implements EventHandler {

	private LeftDeepPlan lastPlan = null;
	

	/**
	 * Default constructor
	 */
	public PlanPrinter() {
		super();
	}


	/**
	 * Event-triggered, records information about the first/best plan found so
	 * far. 
	 * @param plan
	 */
	@Subscribe
	public void process(LeftDeepPlan plan) {
		if (this.lastPlan == null || !this.lastPlan.getCost().equals(plan.getCost())) {
			this.lastPlan = plan;
			PrettyLinearPlanWriter.to(System.out).write(plan);
		}
	}
}
