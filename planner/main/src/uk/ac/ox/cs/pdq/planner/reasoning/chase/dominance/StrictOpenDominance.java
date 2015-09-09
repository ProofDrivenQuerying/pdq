package uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance;

import uk.ac.ox.cs.pdq.cost.estimators.SimpleCostEstimator;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;

/**
 * Open configuration domination. A configuration c dominates a configuration c',
 * if c both cost- and fact- dominates c' with one of the two being strict.
 * When both configurations are open, then a simple plan cost estimator is used
 * to assess the configurations' costs;
 * otherwise, the costs of their corresponding (closed) plans are considered.
 *
 * @author Efthymia Tsamoura
 */
public class StrictOpenDominance implements Dominance<ChaseConfiguration> {

	/** True if we allow open to closed comparison*/
	private final boolean openToClosedComparison;
	/** Performs fact dominance */
	private final FactDominance factDominance = new FastFactDominance(false);
	/** Performs strict fact dominance */
	private final FactDominance strictFactDominance = new FastFactDominance(true);
	/** Simple plan cost estimator*/
	private final SimpleCostEstimator<Plan> estimator;

	/**
	 * Constructor for StrictOpenDominance.
	 * @param simpleCostEstimator SimpleCostEstimator<Plan>
	 * @param openToClosedComparison boolean
	 */
	public StrictOpenDominance(SimpleCostEstimator<Plan> simpleCostEstimator, boolean openToClosedComparison) {
		this.estimator = simpleCostEstimator;
		this.openToClosedComparison = openToClosedComparison;
	}

	/**
	 * @param source DAGConfiguration<?>
	 * @param target DAGConfiguration<?>
	 * @return Boolean
	 * @see uk.ac.ox.cs.pdq.dominance.detectors.Dominance#isDominated(ChaseConfiguration, ChaseConfiguration)
	 */
	@Override
	public boolean isDominated(ChaseConfiguration source, ChaseConfiguration target) {
		boolean strictlyFactDominated = this.strictFactDominance.isDominated(source, target);
		boolean factDominated = this.factDominance.isDominated(source, target);
		boolean strictlyCostDominated = false;
		boolean costDominated = false;

		if(		(source.getCostEstimator() instanceof SimpleCostEstimator 
				&& target.getCostEstimator() instanceof SimpleCostEstimator) ||
				(source.isClosed() && target.isClosed())
				) {
			strictlyCostDominated = source.getPlan().getCost().greaterThan(target.getPlan().getCost());
			costDominated = source.getPlan().getCost().greaterOrEquals(target.getPlan().getCost());
		}
		else if(this.openToClosedComparison || (!source.isClosed() && !target.isClosed())) {
			strictlyCostDominated = this.estimator.estimateCost(source.getPlan()).greaterThan(this.estimator.estimateCost(target.getPlan()));
			costDominated = this.estimator.estimateCost(source.getPlan()).greaterOrEquals(this.estimator.estimateCost(target.getPlan()));
		}

		if((!(source instanceof ApplyRule) && strictlyFactDominated && costDominated) ||
				(!(source instanceof ApplyRule) && factDominated && strictlyCostDominated)) {
			return true;
		}

		if (!(source instanceof ApplyRule) &&
				!source.isClosed() && !target.isClosed() &&
				source.isEquivalentTo(target) &&
				this.estimator.estimateCost(source.getPlan()).equals(this.estimator.estimateCost(target.getPlan())) ) {
			return true;
		}
		return false;
	}

	/**
	 * @return StrictOpenDominance
	 * @see uk.ac.ox.cs.pdq.dag.dominance.Dominance#clone()
	 */
	@Override
	public StrictOpenDominance clone() {
		return new StrictOpenDominance(this.estimator.clone(), this.openToClosedComparison);
	}
}