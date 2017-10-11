package uk.ac.ox.cs.pdq.planner.logging.performance;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.logging.StatisticsLogger;
import uk.ac.ox.cs.pdq.planner.Explorer;
import uk.ac.ox.cs.pdq.util.EventHandler;

import com.google.common.eventbus.Subscribe;

/**
 * Statistics logger to high-level explorer informations.
 *
 * @author Julien Leblay
 */
public class EventDrivenExplorerStatistics extends StatisticsLogger implements EventHandler {

	/**  The number of round the explorer has performed so far. */
	protected int round = 0;

	/**  The cost of the best plan so far. */
	protected Cost bestCost = null;

	/**  The best plan so far. */
	protected RelationalTerm bestPlan = null;

	/**  The total exploration time. */
	protected double milliTotal = 0;

	/**  The time to the first match found. */
	protected double milliFirstMatch = 0;

	/**  The time to the best match found so far. */
	protected double milliBestMatch = 0;

	/**  The number of rounds to the first match. */
	protected int roundFirstMatch = 0;

	/**  The number of rounds to the best match found so far. */
	protected int roundBestMatch = 0;

	/**
	 * Event-triggered, records the number of rounds and total time of the
	 * given explorer.
	 *
	 * @param explorer the explorer
	 */
	@Subscribe
	public void process(Explorer explorer) {
		this.round = explorer.getRounds();
		this.milliTotal = explorer.getElapsedTime() / 1e6;
	}

	/**
	 * Event-triggered, records information about the first/best plan found so
	 * far.
	 * @param plan Plan
	 */
	@Subscribe
	public void process(RelationalTerm plan, Cost cost) {
		if (this.bestPlan == null || cost.lessThan(this.bestCost)) {
			this.roundBestMatch = this.round;
			this.milliBestMatch = this.milliTotal;
		}
		this.bestPlan = plan;
		this.bestCost = cost;
		if (this.roundFirstMatch == 0) {
			this.roundFirstMatch = this.round;
			this.milliFirstMatch = this.milliTotal;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.logging.performance.StatisticsLogger#makeHeader()
	 */
	@Override
	public String makeHeader() {
		StringBuilder result = new StringBuilder();
		result.append(PlannerStatKeys.ROUNDS).append(FIELD_SEPARATOR);
		result.append(PlannerStatKeys.MILLI_TOTAL).append(FIELD_SEPARATOR);
		result.append(PlannerStatKeys.ROUND_FIRST_MATCH).append(FIELD_SEPARATOR);
		result.append(PlannerStatKeys.MILLI_FIRST_MATCH).append(FIELD_SEPARATOR);
		result.append(PlannerStatKeys.ROUND_BEST_MATCH).append(FIELD_SEPARATOR);
		result.append(PlannerStatKeys.MILLI_BEST_MATCH).append(FIELD_SEPARATOR);
		result.append(PlannerStatKeys.BEST_COST).append(FIELD_SEPARATOR);
		//		result.append(StatKeys.BEST_PLAN).append(FIELD_SEPARATOR);
		return result.toString();

	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.logging.performance.StatisticsLogger#makeLine()
	 */
	@Override
	public String makeLine() {
		StringBuilder result = new StringBuilder();
		result.append(this.round).append(FIELD_SEPARATOR);
		result.append(this.milliTotal).append(FIELD_SEPARATOR);
		result.append(this.roundFirstMatch).append(FIELD_SEPARATOR);
		result.append(this.milliFirstMatch).append(FIELD_SEPARATOR);
		result.append(this.roundBestMatch).append(FIELD_SEPARATOR);
		result.append(this.milliBestMatch).append(FIELD_SEPARATOR);
		result.append(this.bestCost).append(FIELD_SEPARATOR);
		//		result.append(this.bestPlan).append(FIELD_SEPARATOR);
		return result.toString();

	}
}
