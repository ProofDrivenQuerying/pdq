package uk.ac.ox.cs.pdq.planner.dominance;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.SimpleCostEstimator;
import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;

// TODO: Auto-generated Javadoc
/**
 * Closed dominance. A closed configuration c dominates a closed configuration c', if c both cost- and fact- dominates c'.
 *
 * @author Efthymia Tsamoura
 */
public class ClosedDominance implements Dominance{

	/** The cost estimator. */
	private final CostEstimator costEstimator;
	
	/** The fact dominance. */
	private final FactDominance factDominance;

	/**
	 * Instantiates a new closed dominance.
	 *
	 * @param costEstimator the cost estimator
	 */
	public ClosedDominance(CostEstimator costEstimator){
		Preconditions.checkNotNull(costEstimator);
		this.costEstimator = costEstimator;
		this.factDominance = new FastFactDominance(false);
	}

	/**
	 * Constructor for ClosedDominance.
	 *
	 * @param costEstimator the cost estimator
	 * @param factDominance FactDominance<DAGConfiguration<?>>
	 */
	public ClosedDominance(CostEstimator costEstimator, FactDominance factDominance){
		Preconditions.checkNotNull(factDominance);
		Preconditions.checkNotNull(costEstimator);
		this.costEstimator = costEstimator;
		this.factDominance = factDominance;
	}

	/**
	 * Checks if is dominated.
	 *
	 * @param source DAGConfiguration<?>
	 * @param target DAGConfiguration<?>
	 * @return Boolean
	 * @see uk.ac.ox.cs.pdq.dominance.detectors.Dominance#isDominated(DAGConfiguration<?>, DAGConfiguration<?>)
	 */
	@Override
	public boolean isDominated(Configuration source, Configuration target) {
		Preconditions.checkNotNull(source);
		Preconditions.checkNotNull(target);
		if(!(source instanceof ApplyRule)
				&& source.getPlan().isClosed()
				&& target.getPlan().isClosed()
				&& source.getCost().greaterThan(target.getCost())
				&& this.factDominance.isDominated(source, target) ) {
			return true;
		} else if(!(source instanceof ApplyRule)
				&& this.costEstimator instanceof SimpleCostEstimator
				&& source.getCost().greaterThan(target.getCost())
				&& this.factDominance.isDominated(source, target) ) {
			return true;
		} else {
			return false;
		}
	}

//	/**
//	 * Clone.
//	 *
//	 * @return ClosedDominance
//	 * @see uk.ac.ox.cs.pdq.dag.dominance.Dominance#clone()
//	 */
//	@Override
//	public ClosedDominance clone() {
//		return new ClosedDominance(this.costEstimator.clone(), this.factDominance.clone());
//	}
}