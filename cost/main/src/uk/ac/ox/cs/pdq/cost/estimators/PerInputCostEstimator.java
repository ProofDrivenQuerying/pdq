package uk.ac.ox.cs.pdq.cost.estimators;

import static uk.ac.ox.cs.pdq.cost.CostStatKeys.COST_ESTIMATION_COUNT;
import static uk.ac.ox.cs.pdq.cost.CostStatKeys.COST_ESTIMATION_TIME;

import java.util.Collection;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.AlgebraUtilities;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * A simple cost estimator.
 * According to this implementation, the cost of a plan equals the sum of the costs of the
 * associated accesses
 *
 * @author Efthymia Tsamoura
 * @param <P> the generic type
 */
public class PerInputCostEstimator implements SimpleCostEstimator{

	/** The stats. */
	protected final StatisticsCollector stats;

	/**
	 * Default constructor.
	 */
	public PerInputCostEstimator() {
		this(null);
	}


	/**
	 * Default constructor.
	 *
	 * @param stats StatisticsCollector
	 */
	public PerInputCostEstimator(StatisticsCollector stats) {
		this.stats = stats;
	}

	/**
	 * Clone.
	 *
	 * @return DefaultSimpleCostEstimator<P,S>
	 * @see uk.ac.ox.cs.pdq.cost.estimators.SimpleCostEstimator#clone()
	 */
	@Override
	public PerInputCostEstimator clone() {
		return (PerInputCostEstimator) (this.stats == null ? new PerInputCostEstimator(null) : new PerInputCostEstimator(this.stats.clone()));
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
			Preconditions.checkState(access.getAccessMethod() != null);
			RelationMetadata metadata = access.getRelation().getMetadata();
			totalCost += metadata.getPerInputTupleCost(access.getAccessMethod()).getValue().doubleValue();
		}
		if(this.stats != null){this.stats.stop(COST_ESTIMATION_TIME);}
		if(this.stats != null){this.stats.increase(COST_ESTIMATION_COUNT, 1);}
		return new DoubleCost(totalCost);
	}

}
