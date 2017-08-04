package uk.ac.ox.cs.pdq.cost.estimators;

import static uk.ac.ox.cs.pdq.cost.logging.CostStatKeys.COST_ESTIMATION_COUNT;
import static uk.ac.ox.cs.pdq.cost.logging.CostStatKeys.COST_ESTIMATION_TIME;

import java.util.Collection;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.AlgebraUtilities;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.logging.StatisticsCollector;

// TODO: Auto-generated Javadoc
/**
 * A simple cost estimator.
 * According to this implementation, the cost of a plan equals the sum of the costs of the
 * associated accesses
 *
 * @author Efthymia Tsamoura
 */
public class PerInputCostEstimator implements OrderIndependentCostEstimator{

	/** The stats. */
	protected final StatisticsCollector stats;
	
	/**  The database statistics. */
	protected final Catalog catalog;

	/**
	 * Default constructor.
	 *
	 * @param stats StatisticsCollector
	 */
	public PerInputCostEstimator(StatisticsCollector stats, Catalog catalog) {
		this.stats = stats;
		this.catalog = catalog;
	}

	/**
	 * Clone.
	 *
	 * @return DefaultSimpleCostEstimator<P,S>
	 * @see uk.ac.ox.cs.pdq.cost.estimators.OrderIndependentCostEstimator#clone()
	 */
	@Override
	public PerInputCostEstimator clone() {
		return (PerInputCostEstimator) (this.stats == null ? new PerInputCostEstimator(null, this.catalog.clone()) : new PerInputCostEstimator(this.stats.clone(), this.catalog.clone()));
	}

	/**
	 * Cost.
	 *
	 * @param term P
	 * @return DoubleCost
	 * @see uk.ac.ox.cs.pdq.cost.estimators.CostEstimator#cost(P)
	 */
	@Override
	public DoubleCost cost(RelationalTerm term) {
		DoubleCost result = this.cost(AlgebraUtilities.getAccesses(term));
		return result;
	}

	/**
	 * Cost.
	 *
	 * @param accesses Collection<AccessOperator>
	 * @return DoubleCost
	 * @see uk.ac.ox.cs.pdq.costs.SimpleCostEstimator#cost(Collection<AccessOperator>)
	 */
	@Override
	public DoubleCost cost(Collection<AccessTerm> accesses) {
		if(this.stats != null){this.stats.start(COST_ESTIMATION_TIME);}
		double totalCost = 0;
		for (AccessTerm access: accesses) {
			Assert.assertNotNull(access.getAccessMethod());
			totalCost += this.catalog.getCost(access.getRelation(), access.getAccessMethod());
		}
		if(this.stats != null){this.stats.stop(COST_ESTIMATION_TIME);}
		if(this.stats != null){this.stats.increase(COST_ESTIMATION_COUNT, 1);}
		return new DoubleCost(totalCost);
	}

}
