package uk.ac.ox.cs.pdq.planner.explorer;


import java.sql.SQLException;
import java.util.Properties;

import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.cost.estimators.AccessCountCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.LengthBasedCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.PerInputCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.TotalERSPICostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.WhiteBoxCostEstimator;
import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.cost.statistics.SimpleCatalog;
import uk.ac.ox.cs.pdq.cost.statistics.estimators.CardinalityEstimator;
import uk.ac.ox.cs.pdq.cost.statistics.estimators.CardinalityEstimatorFactory;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

/**
 * A factory of cost estimation objects
 *
 * @author Julien Leblay
 * @author Efthymia Tsamoura 
 *
 */
public class CostEstimatorFactory {

	/**
	 * 
	 * @param planParams
	 * @param costParams
	 * @param schema
	 * @return
	 * @throws SQLException
	 */
	public static <P extends Plan> CostEstimator<P> getEstimator(PlannerParameters planParams, CostParameters costParams, Schema schema) throws SQLException {
		return getInstance(null, false, planParams, costParams, schema);
	}

	/**
	 * 
	 * @param eventBus
	 * @param collectStats
	 * @param planParams
	 * @param costParams
	 * @param schema
	 * @return
	 * @throws SQLException
	 */
	public static <P extends Plan> CostEstimator<P> getInstance(
			EventBus eventBus, 
			boolean collectStats,
			PlannerParameters planParams, 
			CostParameters costParams, 
			Schema schema) throws SQLException {
		Preconditions.checkArgument(costParams.getCostType() != null, "Cost type param must is not defined.");
		Properties properties = null;
//		boolean isLinear = planParams.getPlannerType().equals(PlannerTypes.LINEAR_GENERIC) ||
//				planParams.getPlannerType().equals(PlannerTypes.LINEAR_KCHASE) ||
//				planParams.getPlannerType().equals(PlannerTypes.LINEAR_OPTIMIZED);
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
//				if (!isLinear) {
//					return new uk.ac.ox.cs.pdq.planner.dag.cost.PostgresqlBlackBoxEstimator(eventBus, collectStats, properties, costParams.getBlackBoxQueryType());
//				}
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
	 * @param schema
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
