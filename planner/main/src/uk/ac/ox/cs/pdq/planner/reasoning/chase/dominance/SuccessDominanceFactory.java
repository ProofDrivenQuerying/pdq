package uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance;

import uk.ac.ox.cs.pdq.cost.estimators.AccessCountCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.SimpleCostEstimator;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.SuccessDominanceTypes;
import uk.ac.ox.cs.pdq.util.Costable;

/**
 * Creates success dominance detectors using the input parameters.
 * The available options are:
 * 		uk.ac.ox.cs.pdq.dominance.ClosedPlanCostDominance
 *
 * @author Efthymia Tsamoura
 */
public class SuccessDominanceFactory<P extends Costable> {

	private final CostEstimator<P> estimator;
	private final SuccessDominanceTypes type;

	/**
	 * Constructor for SuccessDominanceFactory.
	 * @param estimator CostEstimator<P>
	 * @param type SuccessDominanceTypes
	 */
	public SuccessDominanceFactory(CostEstimator<P> estimator, SuccessDominanceTypes type) {
		this.estimator = estimator;
		this.type = type;
	}

	/**
	 * @return SuccessDominance
	 */
	public SuccessDominance getInstance() {
		switch(this.type) {
		case CLOSED:
			return new ClosedSuccessDominance(this.estimator instanceof SimpleCostEstimator);
		case OPEN:
			SimpleCostEstimator<Plan> sc0 = new AccessCountCostEstimator<>();
			return new OpenSuccessDominance(this.estimator instanceof SimpleCostEstimator, sc0);
		default:
			return new ClosedSuccessDominance(this.estimator instanceof SimpleCostEstimator);
		}
	}
}
