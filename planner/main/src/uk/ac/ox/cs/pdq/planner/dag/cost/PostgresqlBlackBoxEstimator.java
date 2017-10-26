package uk.ac.ox.cs.pdq.planner.dag.cost;

import static uk.ac.ox.cs.pdq.cost.logging.CostStatKeys.COST_ESTIMATION_COUNT;
import static uk.ac.ox.cs.pdq.cost.logging.CostStatKeys.COST_ESTIMATION_TIME;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.CostParameters.BlackBoxQueryTypes;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.cost.estimators.OrderDependentCostEstimator;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.logging.StatisticsCollector;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.cost.sql.DAGConfigurationToSQLTranslator;

/**
 * Translates a configuration directly to an SQL
 * query which is further provided to PostgreSQL for cost analysis.
 *
 * @author Efthymia Tsamoura
 */
public class PostgresqlBlackBoxEstimator implements OrderDependentCostEstimator {

	/** The stats. */
	protected final StatisticsCollector stats;

	/**s
	 * The regular expression used to retrieve the costs from the answer of
	 * an EXPLAIN ANALYZE query to postgresql.
	 */
	private static final String COST_REGEXP_PATTERN =
			"\\(cost=\\d+\\.\\d+\\.\\.(?<cost>\\d+\\.\\d+)\\s.*\\)";

	/** Properties featuring database connection details. */
	protected final Properties properties;

	/**  The query type use by this estimator. */
	protected final BlackBoxQueryTypes queryType;

	/**
	 * Default constructor.
	 *
	 * @param eventBus EventBus
	 * @param collectStats boolean
	 * @param prop the prop
	 * @throws SQLException the SQL exception
	 */
	public PostgresqlBlackBoxEstimator(EventBus eventBus, boolean collectStats, Properties prop) throws SQLException {
		this(eventBus, collectStats, prop, BlackBoxQueryTypes.DEFAULT);
	}

	/**
	 * Default constructor.
	 *
	 * @param eventBus EventBus
	 * @param collectStats boolean
	 * @param prop the prop
	 * @param type BlackBoxQueryTypes
	 */
	public PostgresqlBlackBoxEstimator(EventBus eventBus, boolean collectStats, Properties prop, BlackBoxQueryTypes type) {
		this(new StatisticsCollector(collectStats, eventBus), prop, type);
	}

	/**
	 * Default constructor.
	 *
	 * @param stats StatisticsCollector
	 * @param prop the prop
	 * @param type the type
	 */
	public PostgresqlBlackBoxEstimator(StatisticsCollector stats, Properties prop, BlackBoxQueryTypes type) {
		this.stats = stats;
		this.properties = prop;
		this.queryType = type;
	}

	/**
	 * Clone.
	 *
	 * @return PostgresqlBlackBoxEstimator<P,S>
	 * @see uk.ac.ox.cs.pdq.plan.cost.ConfigurationCostEstimator#clone()
	 */
	@Override
	public PostgresqlBlackBoxEstimator clone() {
		return new PostgresqlBlackBoxEstimator(this.stats.clone(), this.properties, this.queryType);
	}

	/**
	 * Gets the connection.
	 *
	 * @return a connection database connection for the given properties.
	 * @throws SQLException the SQL exception
	 */
	public Connection getConnection() throws SQLException {
		String url = this.properties.getProperty("url");
		String database = this.properties.getProperty("database");
		String username = this.properties.getProperty("username");
		String password = this.properties.getProperty("password");
		return DriverManager.getConnection(url + database, username, password);
	}

//	/**
//	 * Cost.
//	 *
//	 * @param configuration DAGConfiguration<S>
//	 * @return Cost
//	 * @see uk.ac.ox.cs.pdq.costs.DAGConfigurationCostEstimator#cost(DAGConfiguration<S>)
//	 */
//	@Override
//	public Cost cost(DAGChaseConfiguration configuration) {
//		configuration.getPlan().setCost(this.estimateCost(configuration));
//		return configuration.getPlan().getCost();
//	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.cost.estimators.CostEstimator#estimateCost(uk.ac.ox.cs.pdq.util.Costable)
	 */
//	public Cost estimateCost(DAGChaseConfiguration configuration) {
	public Cost cost(DAGChaseConfiguration configuration,Schema schema) {
		double result = Double.POSITIVE_INFINITY;
		if(this.stats != null){this.stats.start(COST_ESTIMATION_TIME);}
		if (configuration.isClosed()) {
			StringBuilder sql = new StringBuilder("EXPLAIN ");
			switch (this.queryType) {
			case SQL_WITH:
				throw new java.lang.UnsupportedOperationException();
			default:
				sql.append(new DAGConfigurationToSQLTranslator(configuration,schema).getSql());
				break;
			}
			try(
					Connection conn = this.getConnection();
					Statement stmt = conn.createStatement();
					ResultSet rs = stmt.executeQuery(sql.toString())) {
				if (rs.next()) {
					String s = rs.getString(1);
					Pattern p = Pattern.compile(COST_REGEXP_PATTERN);
					Matcher m = p.matcher(s);
					if (m.find()) {
						String cost = m.group("cost");
						result = Double.valueOf(cost);
					}
				}
			} catch (SQLException e) {
				throw new IllegalStateException(e.getMessage() + " for " + sql + "\n" + configuration);
			}
		}
		if(this.stats != null){this.stats.stop(COST_ESTIMATION_TIME);}
		if(this.stats != null){this.stats.increase(COST_ESTIMATION_COUNT, 1);}
		return new DoubleCost(result);
	}

	@Override
	public Cost cost(RelationalTerm plan) {
		return null;
	}

}
