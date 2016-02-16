package uk.ac.ox.cs.pdq.planner.dominance;

import uk.ac.ox.cs.pdq.plan.Plan;

// TODO: Auto-generated Javadoc
/**
 * Success dominance.
 *
 * @author Efthymia Tsamoura
 */
public abstract class SuccessDominance {

	/**  True if we use a simple cost function to compare plans. */
	private final boolean simpleFunction;

	/**
	 * Constructor for SuccessDominance.
	 * @param simpleFunction Boolean
	 */
	public SuccessDominance(boolean simpleFunction) {
		this.simpleFunction = simpleFunction;
	}
	
	/**
	 * Checks if is dominated.
	 *
	 * @param source the source
	 * @param target the target
	 * @return true if the source plan is success dominated by the target
	 */
	public abstract boolean isDominated(Plan source, Plan target);

	/**
	 * Clone.
	 *
	 * @return SuccessDominance
	 */
	@Override
	public abstract SuccessDominance clone();

	/**
	 * Simple function.
	 *
	 * @return true if we use a simple cost function to compare plans
	 */
	public boolean simpleFunction() {
		return this.simpleFunction;
	}
}
