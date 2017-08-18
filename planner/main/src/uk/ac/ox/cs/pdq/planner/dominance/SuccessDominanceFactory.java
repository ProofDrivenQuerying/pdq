package uk.ac.ox.cs.pdq.planner.dominance;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.cost.estimators.AccessCountCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.OrderIndependentCostEstimator;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.SuccessDominanceTypes;

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

	//private final SuccessDominanceTypes type;
	/** The estimator. */
	private final OrderIndependentCostEstimator estimator = new AccessCountCostEstimator(null);

	/**
	 * Constructor for DominanceFactory.
	 *
	 * @param type DominanceTypes
	 * @param costEstimator the cost estimator
	 */
	public SuccessDominanceFactory(SuccessDominanceTypes type) {
		Preconditions.checkNotNull(type);
		//this.type = type;
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
