package uk.ac.ox.cs.pdq.cost.estimators;

import java.util.Collection;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.plan.AccessCommand;
import uk.ac.ox.cs.pdq.plan.Command;
import uk.ac.ox.cs.pdq.plan.CommandToTGDTranslator;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.plan.DoubleCost;
import uk.ac.ox.cs.pdq.plan.SequentialPlan;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.plan.ToNormalisedPlanTranslator;
import uk.ac.ox.cs.pdq.util.Table;

import com.google.common.base.Preconditions;


/**
 * The cost of the plan equals the total number of output tuples per access 
 * @author Efthymia Tsamoura
 *
 */
public class TotalAccessCostEstimator<P extends Plan> implements BlackBoxCostEstimator<P>{

	protected final StatisticsCollector stats;

	private static Logger log = Logger.getLogger(TotalAccessCostEstimator.class);

	/** The database statistics */
	protected final Catalog catalog;

	/**
	 * 
	 * @param eventBus
	 * @param collectStats
	 * @param catalog
	 * 		The database statistics
	 */
	public TotalAccessCostEstimator(Catalog catalog) {
		this(null, catalog);
	}

	/**
	 * 
	 * @param stats
	 * @param catalog
	 * 		The database statistics
	 */
	public TotalAccessCostEstimator(StatisticsCollector stats, Catalog catalog) {
		this.stats = stats;
		this.catalog = catalog;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public TotalAccessCostEstimator<P> clone() {
		return (TotalAccessCostEstimator<P>) (this.stats == null ? new TotalAccessCostEstimator<>(null, this.catalog.clone()) : new TotalAccessCostEstimator<>(this.stats.clone(), this.catalog.clone()));
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.costs.AbstractCostEstimator#cost(uk.ac.ox.cs.pdq.plan.Plan)
	 */
	@Override
	public DoubleCost cost(P plan) {
		Preconditions.checkArgument(plan instanceof DAGPlan);
		DoubleCost result = this.estimateCost(plan);
		plan.setCost(result);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.costs.AbstractCostEstimator#estimateCost(uk.ac.ox.cs.pdq.plan.Plan)
	 */
	@Override
	public DoubleCost estimateCost(P plan) {
		//Translate plan to a normalised plan
		SequentialPlan normalised = new ToNormalisedPlanTranslator().translate((RelationalOperator) plan.getEffectiveOperator());
		double totalCost = 0.0;
		for(Command command:normalised.getCommands()) {
			if(command instanceof AccessCommand) {	
				//Get the input table
				Table input = ((AccessCommand) command).getInput();

				//Call the constraint-aware cardinality estimator

				//Call the command-aware cardinality estimator

				//Call the simple cardinality estimator

				double cardinality = 1.0;


				double cost = this.catalog.getERPSI(((AccessCommand) command).getRelation(), ((AccessCommand) command).getMethod(), ((AccessCommand) command).getStaticInputs());
						totalCost += cost;
			}
		}
		return new DoubleCost(totalCost);
	}




}
