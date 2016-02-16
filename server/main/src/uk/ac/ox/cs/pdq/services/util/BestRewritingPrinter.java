package uk.ac.ox.cs.pdq.services.util;


import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.plan.Cost;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.rewrite.RewriterException;
import uk.ac.ox.cs.pdq.services.logicblox.cost.LogicBloxDelegateCostEstimator;
import uk.ac.ox.cs.pdq.services.logicblox.rewrite.DAGPlanToConjunctiveQuery;

import com.google.common.eventbus.Subscribe;

// TODO: Auto-generated Javadoc
/**
 * Prints cost to the default loggers, iff better that the last observed cost.
 * @author Julien LEBLAY
 */
public class BestRewritingPrinter implements EventHandler {

	/** Logger. */
	static final Logger log = Logger.getLogger(LogicBloxDelegateCostEstimator.class);

	/** The cost observed so far. */
	private Cost bestCost = null;
	
	/**
	 * Prints cost to the default loggers, iff better that the last observed 
	 * cost.
	 * @param plan DAGPlan
	 */
	@Subscribe
	public void print(DAGPlan plan) {
		if (plan != null && plan.getCost() != null
				&& (bestCost == null || plan.getCost().lessThan(bestCost))) {
			try {
				log.info("\tCost: " + plan.getCost() + " = " + plan
						.rewrite(new DAGPlanToConjunctiveQuery()));
			} catch (RewriterException e) {
				log.info("\tCost: " + plan.getCost());
				log.warn(e);
			}
		}
	}
}
