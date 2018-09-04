package uk.ac.ox.cs.pdq.ui.event;

import uk.ac.ox.cs.pdq.util.EventHandler;
//import uk.ac.ox.cs.pdq.io.pretty.PrettyLeftDeepPlanWriter;
//import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;

import com.google.common.eventbus.Subscribe;

// TODO: Auto-generated Javadoc
/**
 * Statistics logger to high-level explorer informations.
 * 
 * @author Julien Leblay
 */
public class PlanPrinter implements EventHandler {

	/** The last plan. */
// MR	private LeftDeepPlan lastPlan = null;
	

	/**
	 * Default constructor.
	 */
	public PlanPrinter() {
		super();
	}


	/**
	 * Event-triggered, records information about the first/best plan found so
	 * far. 
	 *
	 * @param plan the plan
	 */
/* MR	@Subscribe
	public void process(LeftDeepPlan plan) {
		if (this.lastPlan == null || !this.lastPlan.getCost().equals(plan.getCost())) {
			this.lastPlan = plan;
			PrettyLeftDeepPlanWriter.to(System.out).write(plan);
		}
	}*/
}
