package uk.ac.ox.cs.pdq.planner.dag.cost;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.plan.Cost;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;

// TODO: Auto-generated Javadoc
/**
 * The Class ConfigurationCostEstimator.
 *
 * @author Efthymia Tsamoura
 * @deprecated 
 */
public class ConfigurationCostEstimator implements CostEstimator<Configuration> {

	/** The estimator. */
	private final CostEstimator<Plan> estimator;
	
	/**
	 * Instantiates a new configuration cost estimator.
	 *
	 * @param ce the ce
	 */
	public ConfigurationCostEstimator(CostEstimator<Plan> ce) {
		this.estimator = ce;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.cost.estimators.CostEstimator#cost(uk.ac.ox.cs.pdq.util.Costable)
	 */
	@Override
	public Cost cost(Configuration config) {
		return estimator.cost(config.getPlan());
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.cost.estimators.CostEstimator#estimateCost(uk.ac.ox.cs.pdq.util.Costable)
	 */
	@Override
	public Cost estimateCost(Configuration config) {
		return estimator.estimateCost(config.getPlan());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public CostEstimator<Configuration> clone() {
		return new ConfigurationCostEstimator(this.estimator);
	}
}
