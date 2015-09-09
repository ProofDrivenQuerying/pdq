package uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance;

import uk.ac.ox.cs.pdq.cost.estimators.SimpleCostEstimator;
import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;

import com.google.common.base.Preconditions;

/**
 * Closed domination. A closed configuration c dominates a closed configuration c', if c both cost- and fact- dominates c'.
 *
 * @author Efthymia Tsamoura
 */
public class ClosedDominance implements Dominance<ChaseConfiguration>{

	private final FactDominance factDominance;

	public ClosedDominance(){
		this.factDominance = new FastFactDominance(false);
	}

	/**
	 * Constructor for ClosedDominance.
	 * @param factDominance FactDominance<DAGConfiguration<?>>
	 */
	public ClosedDominance(FactDominance factDominance){
		Preconditions.checkNotNull(factDominance);
		this.factDominance = factDominance;
	}

	/**
	 * @param source DAGConfiguration<?>
	 * @param target DAGConfiguration<?>
	 * @return Boolean
	 * @see uk.ac.ox.cs.pdq.dominance.detectors.Dominance#isDominated(DAGConfiguration<?>, DAGConfiguration<?>)
	 */
	@Override
	public boolean isDominated(ChaseConfiguration source, ChaseConfiguration target) {
		Preconditions.checkNotNull(source);
		Preconditions.checkNotNull(target);
		if(!(source instanceof ApplyRule)
				&& source.isClosed()
				&& target.isClosed()
				&& source.getPlan().getCost().greaterThan(target.getPlan().getCost())
				&& this.factDominance.isDominated(source, target) ) {
			return true;
		} else if(!(source instanceof ApplyRule)
				&& source.getCostEstimator() instanceof SimpleCostEstimator
				&& target.getCostEstimator() instanceof SimpleCostEstimator
				&& source.getPlan().getCost().greaterThan(target.getPlan().getCost())
				&& this.factDominance.isDominated(source, target) ) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @return ClosedDominance
	 * @see uk.ac.ox.cs.pdq.dag.dominance.Dominance#clone()
	 */
	@Override
	public ClosedDominance clone() {
		return new ClosedDominance(this.factDominance.clone());
	}
}