package uk.ac.ox.cs.pdq.cost;


import java.sql.SQLException;

import org.junit.Assert;

import uk.ac.ox.cs.pdq.cost.CostParameters.CostTypes;
import uk.ac.ox.cs.pdq.cost.estimators.CardinalityEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.CountNumberOfAccessedRelationsCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.FixedCostPerAccessCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.LengthBasedCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.NaiveCardinalityEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.QueryExplainCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.TextBookCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.TotalNumberOfOutputTuplesPerAccessCostEstimator;
import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.cost.statistics.SimpleCatalog;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseParameters;
import uk.ac.ox.cs.pdq.reasoningdatabase.ExternalDatabaseManager;

/**
 * A factory of cost estimation objects.
 * 	The following types of plan cost estimators are supported:
	-SIMPLE_CONSTANT: Estimates the cost as the sum of the cost of all accesses in a plan, \n where access cost are provided externally	
	-SIMPLE_RANDOM: Estimates the cost as the sum of the cost of all accesses in a plan, \n where cost are assigned randomly
	-SIMPLE_GIVEN: Estimates the cost as the sum of the cost of all accesses in a plan, \n where cost are measured automatically from the underlying datasources	
	-SIMPLE_COUNT: Estimates the cost as the sum of all accesses in a plan	
	-BLACKBOX: Estimates the cost through some externally defined cost function.\nCurrently, this defaults to the white box cost functions relying on textbox cost estimation techniques	
	-BLACKBOX_DB Estimates the cost by translating the query to SQL and asking its cost to a DBMS. The current implementation supports Postgres 
	-INVERSE_LENGTH: Experimental: estimates the cost as the number of atoms in a plan
	-SIMPLE_ERSPI Estimates the cost as the sum of the estimated result size per invocation associated to each access method used in a plan.
 *
 * @author Julien Leblay
 * @author Efthymia Tsamoura 
 *
 */
public class CostEstimatorFactory {

	/**
	 * Gets the estimator.
	 *
	 * @param  the generic type
	 * @param costParams 		Parameters that specify the type of plan cost estimation object that will be created 
	 * @param schema 		The database schema
	 * @return the estimator
	 * @throws SQLException the SQL exception
	 */
	public static CostEstimator getEstimator(CostParameters costParams, Schema schema) {
		return getInstance(costParams, schema);
	}

	/**
	 * Gets the single instance of CostEstimatorFactory.
	 *
	 * @param  the generic type
	 * @param eventBus the event bus
	 * @param collectStats the collect stats
	 * @param costParams 		Parameters that specify the type of plan cost estimation object that will be created
	 * @param schema 		The database schema
	 * @return single instance of CostEstimatorFactory
	 * @throws SQLException the SQL exception
	 */
	public static CostEstimator getInstance(
			CostParameters costParams, 
			Schema schema) {
		Assert.assertNotNull("Cost type parameter is not defined.", costParams.getCostType());
		CostEstimator result = null;
		
		Catalog catalog = null;
		if(costParams.getCatalog() != null) {
			try {
				catalog = new SimpleCatalog(schema, costParams.getCatalog());
			}catch(Exception e) {
				System.out.println("Error with "+costParams.getCatalog() + " : " + e.getMessage());
				e.printStackTrace();
			}
		} else {
			if (CostTypes.TEXTBOOK.equals(costParams.getCostType())) {
				costParams.setCostType(CostTypes.SIMPLE_CONSTANT);
			}
		}
		
		switch (costParams.getCostType()) {
		case TEXTBOOK:
			CardinalityEstimator card = null;
			switch (costParams.getCardinalityEstimationType()) {
			case NAIVE:
				card = new NaiveCardinalityEstimator(catalog);
				break;
			default:
				throw new IllegalArgumentException("Cardinality estimation " + costParams.getCardinalityEstimationType() + "  not yet supported.");
			}
			result = new TextBookCostEstimator(card);
			break;
		case BLACKBOX_DB:
			try {
				DatabaseParameters dbParams = DatabaseParameters.Empty;
				dbParams.setConnectionUrl(costParams.getBlackBoxConnectionUrl());
				dbParams.setDatabaseDriver(costParams.getBlackBoxDatabaseDriver());
				dbParams.setDatabaseName(costParams.getBlackBoxDatabaseName());
				dbParams.setDatabaseUser(costParams.getBlackBoxDatabaseUser());
				dbParams.setDatabasePassword(costParams.getBlackBoxDatabasePassword());
				dbParams.setUseInternalDatabaseManager(false);
				DatabaseManager dbm = new ExternalDatabaseManager(dbParams);
				dbm.initialiseDatabaseForSchema(schema);
				result = new QueryExplainCostEstimator(dbm);
			} catch (DatabaseException e) {
				e.printStackTrace();
				throw new UnsupportedOperationException("BLACKBOX_DB cost estimator is not currently supported.",e);
			}
			break;
		case NUMBER_OF_OUTPUT_TUPLES_PER_ACCESS:
			Assert.assertNotNull(costParams.getCatalog());
			result = new TotalNumberOfOutputTuplesPerAccessCostEstimator(catalog);
			break;
		case FIXED_COST_PER_ACCESS:
			Assert.assertNotNull(costParams.getCatalog());
			result =  new FixedCostPerAccessCostEstimator(catalog);
			break;
		case INVERSE_LENGTH:
			result =  new LengthBasedCostEstimator();
			break;
		default:
			result =  new CountNumberOfAccessedRelationsCostEstimator();
			break;
		}
		return result;
	}
}
