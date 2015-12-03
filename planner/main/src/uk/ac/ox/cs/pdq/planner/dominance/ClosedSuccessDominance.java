package uk.ac.ox.cs.pdq.planner.dominance;

import uk.ac.ox.cs.pdq.plan.Plan;

/**
 * Closed success domination. A closed plan p success dominates another closed plan p', if p is successful and has cost < the cost of p'.
 *
 * @author Efthymia Tsamoura
 */
public class ClosedSuccessDominance extends SuccessDominance{

	/**
	 * Constructor for ClosedSuccessDominance.
	 * @param simpleFunction Boolean
	 */
	public ClosedSuccessDominance(Boolean simpleFunction) {
		super(simpleFunction);
	}

	/**
	 * @param source Plan
	 * @param target Plan
	 * @return true if the source plan is success dominated by the target
	 */
	@Override
	public boolean isDominated(Plan source, Plan target) {
		if(this.simpleFunction() || (source.isClosed() && target.isClosed())) {
			if(source.getCost().greaterThan(target.getCost())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return ClosedSuccessDominance
	 */
	@Override
	public ClosedSuccessDominance clone() {
		return new ClosedSuccessDominance(this.simpleFunction());
	}

}
