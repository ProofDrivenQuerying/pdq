package uk.ac.ox.cs.pdq.planner.dominance;

import uk.ac.ox.cs.pdq.cost.estimators.SimpleCostEstimator;
import uk.ac.ox.cs.pdq.plan.Plan;

// TODO: Auto-generated Javadoc
/**
 * Open success dominance.
 * A plan p success dominates another closed plan ', if p is successful and has cost < the cost of p'.
 * If either of the plans is open, then a simple plan cost estimator is used to assess their cost;
 * otherwise, the costs of their corresponding (closed) plans are considered.
 *
 * @author Efthymia Tsamoura
 */
public class OpenSuccessDominance extends SuccessDominance{

	/** The estimator. */
	private final SimpleCostEstimator<Plan> estimator;
	
	/** The closed dominance. */
	private final ClosedSuccessDominance closedDominance;

	/**
	 * Constructor for OpenSuccessDominance.
	 * @param simpleFunction Boolean
	 * @param simpleCostEstimator SimpleCostEstimator<Plan>
	 */
	public OpenSuccessDominance(Boolean simpleFunction, SimpleCostEstimator<Plan> simpleCostEstimator) {
		super(simpleFunction);
		this.estimator = simpleCostEstimator;
		this.closedDominance = new ClosedSuccessDominance(this.simpleFunction());
	}

	/**
	 * Checks if is dominated.
	 *
	 * @param source the source
	 * @param target the target
	 * @return true if the source plan is success dominated by the target
	 */
	@Override
	public boolean isDominated(Plan source, Plan target) {
		if(this.closedDominance.isDominated(source, target)) {
			return true;
		}

		if(this.estimator.estimateCost(source).greaterThan(this.estimator.estimateCost(target))) {
			return true;
		}
		return false;
	}

	/**
	 * Clone.
	 *
	 * @return OpenSuccessDominance
	 */
	@Override
	public OpenSuccessDominance clone() {
		return new OpenSuccessDominance(this.simpleFunction(), this.estimator.clone());
	}
}