package uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.SimpleCostEstimator;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.equivalence.FactEquivalence;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.equivalence.FastFactEquivalence;

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

	private final CostEstimator<Plan> costEstimator;
	/** Performs fact dominance */
	private final FactDominance factDominance = new FastFactDominance(false);
	private final FactEquivalence factEquivalence = new FastFactEquivalence();
	/** Performs strict fact dominance */
	private final FactDominance strictFactDominance = new FastFactDominance(true);
	/** Simple plan cost estimator*/
	private final SimpleCostEstimator<Plan> simpleEstimator;
	/** True if we allow open to closed comparison*/
	private final boolean openToClosedComparison;
	
	/**
	 * Constructor for StrictOpenDominance.
	 * @param simpleCostEstimator SimpleCostEstimator<Plan>
	 * @param openToClosedComparison boolean
	 */
	public StrictOpenDominance(CostEstimator<Plan> costEstimator, SimpleCostEstimator<Plan> simpleCostEstimator, boolean openToClosedComparison) {
		Preconditions.checkNotNull(costEstimator);
		Preconditions.checkNotNull(simpleCostEstimator);
		this.costEstimator = costEstimator;
		this.simpleEstimator = simpleCostEstimator;
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
		if(this.costEstimator instanceof SimpleCostEstimator  ||
				(source.isClosed() && target.isClosed())
				) {
			strictlyCostDominated = source.getPlan().getCost().greaterThan(target.getPlan().getCost());
			costDominated = source.getPlan().getCost().greaterOrEquals(target.getPlan().getCost());
		}
		else if(this.openToClosedComparison || (!source.isClosed() && !target.isClosed())) {
			strictlyCostDominated = this.simpleEstimator.estimateCost(source.getPlan()).greaterThan(this.simpleEstimator.estimateCost(target.getPlan()));
			costDominated = this.simpleEstimator.estimateCost(source.getPlan()).greaterOrEquals(this.simpleEstimator.estimateCost(target.getPlan()));
		}

		if((!(source instanceof ApplyRule) && strictlyFactDominated && costDominated) ||
				(!(source instanceof ApplyRule) && factDominated && strictlyCostDominated)) {
			return true;
		}

		if (!(source instanceof ApplyRule) &&
				!source.isClosed() && !target.isClosed() &&
				this.factEquivalence.isEquivalent(source, target) &&
				this.simpleEstimator.estimateCost(source.getPlan()).equals(this.simpleEstimator.estimateCost(target.getPlan())) ) {
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
		return new StrictOpenDominance(this.costEstimator.clone(), this.simpleEstimator.clone(), this.openToClosedComparison);
	}
}