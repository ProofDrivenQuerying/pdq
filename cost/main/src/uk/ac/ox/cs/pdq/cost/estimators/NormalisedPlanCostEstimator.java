package uk.ac.ox.cs.pdq.cost.estimators;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.cost.statistics.estimators.ConstraintCardinalityEstimator;
import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.plan.AccessCommand;
import uk.ac.ox.cs.pdq.plan.Command;
import uk.ac.ox.cs.pdq.plan.DoubleCost;
import uk.ac.ox.cs.pdq.plan.SequentialPlan;
import uk.ac.ox.cs.pdq.util.Table;


/**
 * The cost of the plan equals the total number of output tuples per access 
 * @author Efthymia Tsamoura
 *
 */
public class NormalisedPlanCostEstimator {

	protected final StatisticsCollector stats;

	private static Logger log = Logger.getLogger(NormalisedPlanCostEstimator.class);

	/** The database statistics */
	protected final Catalog catalog;

	protected final ConstraintCardinalityEstimator cardinalityEstimator; 

	/**
	 * 
	 * @param eventBus
	 * @param collectStats
	 * @param catalog
	 * 		The database statistics
	 */
	public NormalisedPlanCostEstimator(ConstraintCardinalityEstimator cardinalityEstimator, Catalog catalog) {
		this(null, cardinalityEstimator, catalog);
	}

	/**
	 * 
	 * @param stats
	 * @param catalog
	 * 		The database statistics
	 */
	public NormalisedPlanCostEstimator(StatisticsCollector stats, ConstraintCardinalityEstimator cardinalityEstimator, Catalog catalog) {
		this.stats = stats;
		this.catalog = catalog;
		this.cardinalityEstimator = cardinalityEstimator;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public NormalisedPlanCostEstimator clone() {
		return (this.stats == null ? new NormalisedPlanCostEstimator(null, this.cardinalityEstimator, this.catalog.clone()) : new NormalisedPlanCostEstimator(this.stats.clone(), this.cardinalityEstimator, this.catalog.clone()));
	}


	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.costs.AbstractCostEstimator#estimateCost(uk.ac.ox.cs.pdq.plan.Plan)
	 */
	public DoubleCost estimateCost(SequentialPlan plan) {
		double totalCost = 0.0;
		for(Command command:plan.getCommands()) {
			if(command instanceof AccessCommand) {	
				//Get the input table
				Table input = ((AccessCommand) command).getInput();
				//Call the constraint-aware cardinality estimator
				//Call the command-aware cardinality estimator
				//Call the simple cardinality estimator
				Integer cardinality = this.cardinalityEstimator.cardinality(input, plan, this.catalog);

				double cost = this.catalog.getERPSI(((AccessCommand) command).getRelation(), ((AccessCommand) command).getMethod(), ((AccessCommand) command).getStaticInputs());
				totalCost += cardinality * cost;
			}
		}
		return new DoubleCost(totalCost);
	}




}
