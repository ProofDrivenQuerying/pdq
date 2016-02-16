package uk.ac.ox.cs.pdq.cost;


import java.sql.SQLException;
import java.util.Properties;

import uk.ac.ox.cs.pdq.cost.estimators.AccessCountCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.CardinalityEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.CardinalityEstimatorFactory;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.LengthBasedCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.PerInputCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.TotalERSPICostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.WhiteBoxCostEstimator;
import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.cost.statistics.SimpleCatalog;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.plan.Plan;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

// TODO: Auto-generated Javadoc
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
	 * @param <P> the generic type
	 * @param costParams 		Parameters that specify the type of plan cost estimation object that will be created 
	 * @param schema 		The database schema
	 * @return the estimator
	 * @throws SQLException the SQL exception
	 */
	public static <P extends Plan> CostEstimator<P> getEstimator(CostParameters costParams, Schema schema) throws SQLException {
		return getInstance(null, false, costParams, schema);
	}

	/**
	 * Gets the single instance of CostEstimatorFactory.
	 *
	 * @param <P> the generic type
	 * @param eventBus the event bus
	 * @param collectStats the collect stats
	 * @param costParams 		Parameters that specify the type of plan cost estimation object that will be created
	 * @param schema 		The database schema
	 * @return single instance of CostEstimatorFactory
	 * @throws SQLException the SQL exception
	 */
	public static <P extends Plan> CostEstimator<P> getInstance(
			EventBus eventBus, 
			boolean collectStats,
			CostParameters costParams, 
			Schema schema) throws SQLException {
		Preconditions.checkArgument(costParams.getCostType() != null, "Cost type param must is not defined.");
		Properties properties = null;
		CostEstimator<P> result = null;
		switch (costParams.getCostType()) {
		case BLACKBOX:
			CardinalityEstimator card = CardinalityEstimatorFactory.getInstance(costParams.getCostType(), costParams.getCardinalityEstimationType(), schema);
			result = new WhiteBoxCostEstimator(new StatisticsCollector(collectStats, eventBus), card);
			break;
		case BLACKBOX_DB:
			if ((properties = findRelationalProperties(schema)) != null) {
				String url = properties.getProperty("url");
				String bbUrl = costParams.getBlackBoxConnectionUrl();
				Properties prop = new Properties();
				if (url != null && bbUrl != null) {
					if (url.contains("postgresql") && bbUrl.contains("postgresql")) {
						prop.put("url", costParams.getBlackBoxConnectionUrl());
						prop.put("driver", costParams.getBlackBoxDatabaseDriver());
						prop.put("database", costParams.getBlackBoxDatabaseName());
						prop.put("username", costParams.getBlackBoxDatabaseUser());
						prop.put("password", costParams.getBlackBoxDatabasePassword());
					}
				}
				result = new uk.ac.ox.cs.pdq.cost.estimators.PostgresqlBlackBoxEstimator(new StatisticsCollector(collectStats, eventBus), properties, costParams.getBlackBoxQueryType());
				break;
			}
			throw new UnsupportedOperationException("BLACKBOX_DB cost estimator is supported for the provided schema.");
		case SIMPLE_ERSPI:
			Preconditions.checkNotNull(costParams.getDatabaseCatalog());
			Catalog catalog = new SimpleCatalog(schema, costParams.getDatabaseCatalog());
			result = new TotalERSPICostEstimator(new StatisticsCollector(collectStats, eventBus), catalog);
			break;
		case SIMPLE_CONSTANT:
		case SIMPLE_GIVEN:
			result =  new PerInputCostEstimator(new StatisticsCollector(collectStats, eventBus));
			break;
		case INVERSE_LENGTH:
			result =  new LengthBasedCostEstimator(new StatisticsCollector(collectStats, eventBus));
			break;
		default:
			result =  new AccessCountCostEstimator(new StatisticsCollector(collectStats, eventBus));
			break;
		}
		return result;
	}

	/**
	 * Find relational properties.
	 *
	 * @param schema the schema
	 * @return the schema underlying relational's properties if all of the
	 * relations in the schema have the same properties, null otherwise.
	 */
	private static Properties findRelationalProperties(Schema schema) {
		Properties result = null;
		for (Relation relation: schema.getRelations()) {
			Properties properties = relation.getProperties();
			if (result == null) {
				result = properties;
			} else if (!(properties.get("url").equals(result.get("url"))
					&& properties.get("database").equals(result.get("database")))) {
				return null;
			}
		}
		return result;
	}
}
