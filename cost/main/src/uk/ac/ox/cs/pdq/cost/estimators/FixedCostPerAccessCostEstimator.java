package uk.ac.ox.cs.pdq.cost.estimators;

import java.util.Collection;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.cost.statistics.Catalog;

/**
 * A simple cost estimator.
 * According to this implementation, the cost of a plan equals the sum of the costs of the
 * associated accesses
 *
 * @author Efthymia Tsamoura
 */
public class FixedCostPerAccessCostEstimator implements OrderIndependentCostEstimator{
	
	/**  The database statistics. */
	protected final Catalog catalog;

	/**
	 * Default constructor.
	 *
	 * @param stats StatisticsCollector
	 */
	public FixedCostPerAccessCostEstimator(Catalog catalog) {
		this.catalog = catalog;
	}

	/**
	 * Clone.
	 *
	 * @return DefaultSimpleCostEstimator<P,S>
	 * @see uk.ac.ox.cs.pdq.cost.estimators.OrderIndependentCostEstimator#clone()
	 */
	@Override
	public FixedCostPerAccessCostEstimator clone() {
		return (FixedCostPerAccessCostEstimator) new FixedCostPerAccessCostEstimator(this.catalog.clone());
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
		DoubleCost result = this.cost(term.getAccesses());
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
		double totalCost = 0;
		for (AccessTerm access: accesses) {
			Assert.assertNotNull(access.getAccessMethod());
			totalCost += this.catalog.getCost(access.getRelation(), access.getAccessMethod());
		}
		return new DoubleCost(totalCost);
	}

}
