package uk.ac.ox.cs.pdq.cost.estimators;

import static uk.ac.ox.cs.pdq.cost.logging.CostStatKeys.COST_ESTIMATION_COUNT;
import static uk.ac.ox.cs.pdq.cost.logging.CostStatKeys.COST_ESTIMATION_TIME;

import java.util.Collection;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.AlgebraUtilities;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.logging.StatisticsCollector;


// TODO: Auto-generated Javadoc
/**
 * The cost of the plan equals the total number of output tuples per access .
 *
 * @author Efthymia Tsamoura
 * @param <P> the generic type
 */
public class TotalNumberOfOutputTuplesPerAccessCostEstimator implements OrderIndependentCostEstimator{

	/** The stats. */
	protected final StatisticsCollector stats;

	/**  The database statistics. */
	protected final Catalog catalog;

	/**
	 * Instantiates a new total erspi cost estimator.
	 *
	 * @param stats the stats
	 * @param catalog 		The database statistics
	 */
	public TotalNumberOfOutputTuplesPerAccessCostEstimator(StatisticsCollector stats, Catalog catalog) {
		this.stats = stats;
		this.catalog = catalog;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public TotalNumberOfOutputTuplesPerAccessCostEstimator clone() {
		return (TotalNumberOfOutputTuplesPerAccessCostEstimator) (this.stats == null ? new TotalNumberOfOutputTuplesPerAccessCostEstimator(null, this.catalog.clone()) : new TotalNumberOfOutputTuplesPerAccessCostEstimator(this.stats.clone(), this.catalog.clone()));
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
		if(this.stats != null){this.stats.start(COST_ESTIMATION_TIME);}
		double totalCost = 0.0;
		for(AccessTerm access:accesses) {
			if(access.getNumberOfInputAttributes() ==0) 
				totalCost += this.catalog.getERPSI(access.getRelation(), access.getAccessMethod());
			else 
				totalCost += this.catalog.getERPSI(access.getRelation(), access.getAccessMethod(), access.getInputConstants());
		}
		if(this.stats != null){this.stats.stop(COST_ESTIMATION_TIME);}
		if(this.stats != null){this.stats.increase(COST_ESTIMATION_COUNT, 1);}
		return new DoubleCost(totalCost);
	}
}
