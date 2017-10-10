package uk.ac.ox.cs.pdq.planner.dag.cost;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;

/**
 * The Class ConfigurationCostEstimator.
 *
 * @author Efthymia Tsamoura
 */
public class ConfigurationCostEstimator implements CostEstimator {

	/** The estimator. */
	private final CostEstimator estimator;
	
	/**
	 * Instantiates a new configuration cost estimator.
	 *
	 * @param ce the ce
	 */
	public ConfigurationCostEstimator(CostEstimator ce) {
		this.estimator = ce;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.cost.estimators.CostEstimator#cost(uk.ac.ox.cs.pdq.util.Costable)
	 */
	public Cost cost(Configuration config) {
		return estimator.cost(config.getPlan());
	}

	@Override
	public Cost cost(RelationalTerm plan) {
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public CostEstimator clone() {
		return new ConfigurationCostEstimator(this.estimator);
	}
}
