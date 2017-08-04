package uk.ac.ox.cs.pdq.planner.dominance;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.cost.estimators.OrderIndependentCostEstimator;
import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;

// TODO: Auto-generated Javadoc
/**
 * Closed dominance. A closed configuration c dominates a closed configuration c', if c both cost- and fact- dominates c'.
 *
 * @author Efthymia Tsamoura
 */
public class CostFactDominance implements Dominance{

	/** The cost estimator. */
	private final OrderIndependentCostEstimator costEstimator;
	
	/** The fact dominance. */
	private final FactDominance inputFactDominance;
	
	private final boolean isStrict;

	/**
	 * Constructor for ClosedDominance.
	 *
	 * @param costEstimator the cost estimator
	 * @param inputFactDominance FactDominance<DAGConfiguration<?>>
	 */
	public CostFactDominance(OrderIndependentCostEstimator costEstimator, FactDominance inputFactDominance, boolean isStrict){
		Preconditions.checkNotNull(inputFactDominance);
		Preconditions.checkNotNull(costEstimator);
		this.costEstimator = costEstimator;
		this.inputFactDominance = inputFactDominance;
		this.isStrict = isStrict;
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
			if(this.isStrict) {
				FactDominance strictFactDominance = new FastFactDominance(true);
				boolean strictlyFactDominated = strictFactDominance.isDominated(source, target);
				boolean factDominated = this.inputFactDominance.isDominated(source, target);
				boolean strictlyCostDominated = false;
				boolean costDominated = false;
				
				if(source.getPlan().isClosed() && target.getPlan().isClosed()) {
					strictlyCostDominated = source.getCost().greaterThan(target.getCost());
					costDominated = source.getCost().greaterOrEquals(target.getCost());
				} else if(this.costEstimator != null) {
					strictlyCostDominated = this.costEstimator.cost(source.getPlan()).greaterThan(this.costEstimator.cost(target.getPlan()));
					costDominated = this.costEstimator.cost(source.getPlan()).greaterOrEquals(this.costEstimator.cost(target.getPlan()));
				} 
				if(strictlyFactDominated && costDominated || factDominated && strictlyCostDominated) 
					return true;
				else 
					return false;
			}
			else {
				if(source.getPlan().isClosed() && target.getPlan().isClosed()
						&& source.getCost().greaterThan(target.getCost())
						&& this.inputFactDominance.isDominated(source, target) ) {
					return true;
				} else if(this.costEstimator != null
						&& this.costEstimator.cost(source.getPlan()).greaterThan(this.costEstimator.cost(target.getPlan()))
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
		return new CostFactDominance(this.costEstimator.clone(), this.inputFactDominance.clone(), this.isStrict);
	}
}