package uk.ac.ox.cs.pdq.cost.estimators;

import static uk.ac.ox.cs.pdq.cost.CostStatKeys.COST_ESTIMATION_COUNT;
import static uk.ac.ox.cs.pdq.cost.CostStatKeys.COST_ESTIMATION_TIME;

import java.util.Collection;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.algebra.Access;
import uk.ac.ox.cs.pdq.algebra.DependentAccess;
import uk.ac.ox.cs.pdq.algebra.Scan;
import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.plan.AccessOperator;
import uk.ac.ox.cs.pdq.plan.DoubleCost;
import uk.ac.ox.cs.pdq.plan.Plan;


// TODO: Auto-generated Javadoc
/**
 * The cost of the plan equals the total number of output tuples per access .
 *
 * @author Efthymia Tsamoura
 * @param <P> the generic type
 */
public class TotalERSPICostEstimator<P extends Plan> implements SimpleCostEstimator<P>{

	/** The stats. */
	protected final StatisticsCollector stats;

	/** The log. */
	private static Logger log = Logger.getLogger(TotalERSPICostEstimator.class);

	/**  The database statistics. */
	protected final Catalog catalog;

	/**
	 * Instantiates a new total erspi cost estimator.
	 *
	 * @param catalog 		The database statistics
	 */
	public TotalERSPICostEstimator(Catalog catalog) {
		this(null, catalog);
	}

	/**
	 * Instantiates a new total erspi cost estimator.
	 *
	 * @param stats the stats
	 * @param catalog 		The database statistics
	 */
	public TotalERSPICostEstimator(StatisticsCollector stats, Catalog catalog) {
		this.stats = stats;
		this.catalog = catalog;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public TotalERSPICostEstimator<P> clone() {
		return (TotalERSPICostEstimator<P>) (this.stats == null ? new TotalERSPICostEstimator<>(null, this.catalog.clone()) : new TotalERSPICostEstimator<>(this.stats.clone(), this.catalog.clone()));
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.costs.AbstractCostEstimator#cost(uk.ac.ox.cs.pdq.plan.Plan)
	 */
	@Override
	public DoubleCost cost(P plan) {
		DoubleCost result = this.cost(plan.getAccesses());
		plan.setCost(result);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.costs.AbstractCostEstimator#estimateCost(uk.ac.ox.cs.pdq.plan.Plan)
	 */
	@Override
	public DoubleCost estimateCost(P plan) {
		return this.cost(plan.getAccesses());
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.costs.AbstractCostEstimator#cost(Collection<AccessOperator>)
	 */
	@Override
	public DoubleCost cost(Collection<AccessOperator> accesses) {
		if(this.stats != null){this.stats.start(COST_ESTIMATION_TIME);}
		log.debug(accesses);
		double totalCost = 0.0;
		for(AccessOperator access:accesses) {
			double cost =
					access instanceof Scan || access instanceof Access ? 
							this.catalog.getERPSI(access.getRelation(), access.getAccessMethod()) :
							this.catalog.getERPSI(access.getRelation(), access.getAccessMethod(), ((DependentAccess)access).getStaticInputs());
					totalCost += cost;
		}
		log.debug(totalCost);
		if(this.stats != null){this.stats.stop(COST_ESTIMATION_TIME);}
		if(this.stats != null){this.stats.increase(COST_ESTIMATION_COUNT, 1);}
		return new DoubleCost(totalCost);
	}
}
