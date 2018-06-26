package uk.ac.ox.cs.pdq.cost.estimators;

import java.util.Collection;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.AlgebraUtilities;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.cost.statistics.Catalog;


/**
 * The cost of the plan equals the total number of output tuples per access .
 *
 * @author Efthymia Tsamoura
 * @param <P> the generic type
 */
public class TotalNumberOfOutputTuplesPerAccessCostEstimator implements OrderIndependentCostEstimator{

	/**  The database statistics. */
	protected final Catalog catalog;

	/**
	 * Instantiates a new total erspi (NumberOfOutputTuplesPerAccess) cost estimator.
	 *
	 * @param stats the stats
	 * @param catalog 		The database statistics
	 */
	public TotalNumberOfOutputTuplesPerAccessCostEstimator(Catalog catalog) {
		this.catalog = catalog;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public TotalNumberOfOutputTuplesPerAccessCostEstimator clone() {
		return (TotalNumberOfOutputTuplesPerAccessCostEstimator) new TotalNumberOfOutputTuplesPerAccessCostEstimator(this.catalog.clone());
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.costs.AbstractCostEstimator#cost(uk.ac.ox.cs.pdq.plan.Plan)
	 */
	@Override
	public DoubleCost cost(RelationalTerm plan) {
		DoubleCost result = this.cost(AlgebraUtilities.getAccesses(plan));
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.costs.AbstractCostEstimator#cost(Collection<AccessOperator>)
	 */
	@Override
	public DoubleCost cost(Collection<AccessTerm> accesses) {
		double totalCost = 0.0;
		for(AccessTerm access:accesses) {
			if(access.getInputConstants().size()==0) 
				totalCost += this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(access.getRelation(), access.getAccessMethod());
			else 
				totalCost += this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(access.getRelation(), access.getAccessMethod(), access.getInputConstants());
		}
		return new DoubleCost(totalCost);
	}
}
