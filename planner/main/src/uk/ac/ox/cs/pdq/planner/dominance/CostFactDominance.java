package uk.ac.ox.cs.pdq.planner.dominance;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.cost.estimators.OrderIndependentCostEstimator;
import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;

// TODO: Auto-generated Javadoc
/**
 * Closed dominance. A closed configuration c dominates a closed configuration c', 
 * if c both cost- and fact- dominates c'.
 *
 * @author Efthymia Tsamoura
 */
public class CostFactDominance implements Dominance{

	/** The cost estimator. */
	private final OrderIndependentCostEstimator costEstimatorForOpenPlans;
	
	/** The fact dominance. */
	private final FactDominance inputFactDominance;
	
	private final boolean hasStrictlyFewerFactsOrLessCostCheck;

	/**
	 * Constructor for ClosedDominance.
	 *
	 * @param costEstimatorForOpenPlans the cost estimator
	 * @param inputFactDominance FactDominance<DAGConfiguration<?>>
	 */
	public CostFactDominance(OrderIndependentCostEstimator costEstimatorForOpenPlans, FactDominance inputFactDominance, boolean hasStrictlyFewerFactsOrLessCostCheck){
		Preconditions.checkNotNull(inputFactDominance);
		Preconditions.checkNotNull(costEstimatorForOpenPlans);
		this.costEstimatorForOpenPlans = costEstimatorForOpenPlans;
		this.inputFactDominance = inputFactDominance;
		this.hasStrictlyFewerFactsOrLessCostCheck = hasStrictlyFewerFactsOrLessCostCheck;
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
		
		if(!(source instanceof ApplyRule)) {
			if(this.hasStrictlyFewerFactsOrLessCostCheck) {
				FactDominance strictFactDominance = new FastFactDominance(true);
				boolean strictlyFactDominated = strictFactDominance.isDominated(source, target);
				boolean factDominated = this.inputFactDominance.isDominated(source, target);
				boolean strictlyCostDominated = false;
				if(source.getPlan().isClosed() && target.getPlan().isClosed()) {
					strictlyCostDominated = source.getCost().greaterThan(target.getCost());
					if(strictlyFactDominated && source.getCost().greaterOrEquals(target.getCost()) || strictlyCostDominated && factDominated) 
						return true;
					else 
						return false;
					
					//costDominated = source.getCost().greaterOrEquals(target.getCost());
				} else if(this.costEstimatorForOpenPlans != null) {
					strictlyCostDominated = this.costEstimatorForOpenPlans.cost(source.getPlan()).greaterThan(this.costEstimatorForOpenPlans.cost(target.getPlan()));
					if(strictlyCostDominated && factDominated || strictlyFactDominated && this.costEstimatorForOpenPlans.cost(source.getPlan()).greaterOrEquals(this.costEstimatorForOpenPlans.cost(target.getPlan()))) 
						return true;
					else 
						return false;
				} 
				return false;
			}
			else {
				if(source.getPlan().isClosed() && target.getPlan().isClosed()
						&& source.getCost().greaterThan(target.getCost())
						&& this.inputFactDominance.isDominated(source, target) ) {
					return true;
				} else if(this.costEstimatorForOpenPlans != null
						&& this.costEstimatorForOpenPlans.cost(source.getPlan()).greaterThan(this.costEstimatorForOpenPlans.cost(target.getPlan()))
						&& this.inputFactDominance.isDominated(source, target) ) {
					return true;
				} else {
					return false;
				}
			}	
		}
		else 
			return false;
	}

	/**
	 * Clone.
	 *
	 * @return ClosedDominance
	 * @see uk.ac.ox.cs.pdq.dag.dominance.Dominance#clone()
	 */
	@Override
	public CostFactDominance clone() {
		return new CostFactDominance(this.costEstimatorForOpenPlans.clone(), this.inputFactDominance.clone(), this.hasStrictlyFewerFactsOrLessCostCheck);
	}
}