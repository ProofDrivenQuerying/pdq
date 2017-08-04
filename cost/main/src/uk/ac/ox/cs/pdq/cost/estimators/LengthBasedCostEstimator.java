package uk.ac.ox.cs.pdq.cost.estimators;

import static uk.ac.ox.cs.pdq.cost.logging.CostStatKeys.COST_ESTIMATION_COUNT;
import static uk.ac.ox.cs.pdq.cost.logging.CostStatKeys.COST_ESTIMATION_TIME;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.AlgebraUtilities;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.logging.StatisticsCollector;

// TODO: Auto-generated Javadoc
/**
 * Cost estimator favoring query with more atoms.
 *
 * @author Julien Leblay
 * @param <P> the generic type
 */
public class LengthBasedCostEstimator implements OrderDependentCostEstimator {

	/** The stats. */
	protected final StatisticsCollector stats;
	
	/**
	 * Default constructor. Ignores statistics collection.
	 */
	public LengthBasedCostEstimator() {
		this(null);
	}
	
	/**
	 * Constructor.
	 *
	 * @param stats the stats
	 */
	public LengthBasedCostEstimator(StatisticsCollector stats) {
		this.stats = stats;
	}

	/**
	 * Clone.
	 *
	 * @return LengthBasedCostEstimator<P>
	 * @see uk.ac.ox.cs.pdq.plan.cost.CostEstimator#clone()
	 */
	@Override
	public LengthBasedCostEstimator clone() {
		return (LengthBasedCostEstimator) (this.stats == null ? new LengthBasedCostEstimator(null) : new LengthBasedCostEstimator(this.stats.clone()));
	}

	/**
	 * Cost.
	 *
	 * @param term P
	 * @return Cost
	 * @see uk.ac.ox.cs.pdq.plan.cost.CostEstimator#cost(P)
	 */
	@Override
	public DoubleCost cost(RelationalTerm term) {
		if(this.stats != null){this.stats.start(COST_ESTIMATION_TIME);}
		List<AccessTerm> accesses = new ArrayList<>();
		for (AccessTerm access:AlgebraUtilities.getAccesses(term)) {
			if (!accesses.contains(access)) 
				accesses.add(access);
		}
		DoubleCost result = new DoubleCost(1.0 / accesses.size());
		if(this.stats != null){this.stats.stop(COST_ESTIMATION_TIME);}
		if(this.stats != null){this.stats.increase(COST_ESTIMATION_COUNT, 1);}
		return result;
	}
}
