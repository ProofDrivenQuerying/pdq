package uk.ac.ox.cs.pdq.planner.dominance;

import uk.ac.ox.cs.pdq.cost.estimators.SimpleCostEstimator;

// TODO: Auto-generated Javadoc
/**
 * Creates success dominance detectors using the input parameters.
 * The available options are:
 * 		uk.ac.ox.cs.pdq.dominance.ClosedPlanCostDominance
 *
 * @author Efthymia Tsamoura
 * @param <P> the generic type
 */
public class SuccessDominanceFactory {

	/** The estimator. */
	private final SimpleCostEstimator estimator;

	/**
	 * Constructor for SuccessDominanceFactory.
	 * @param estimator CostEstimator<P>
	 * @param type SuccessDominanceTypes
	 */
	public SuccessDominanceFactory(SimpleCostEstimator estimator) {
		this.estimator = estimator;
	}

	/**
	 * Gets the single instance of SuccessDominanceFactory.
	 *
	 * @return SuccessDominance
	 */
	public SuccessDominance getInstance() {
		return new SuccessDominance(this.estimator);
	}
}
