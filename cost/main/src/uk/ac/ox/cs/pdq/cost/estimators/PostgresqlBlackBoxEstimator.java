package uk.ac.ox.cs.pdq.cost.estimators;

import static uk.ac.ox.cs.pdq.cost.CostStatKeys.COST_ESTIMATION_COUNT;
import static uk.ac.ox.cs.pdq.cost.CostStatKeys.COST_ESTIMATION_TIME;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.cost.CostParameters.BlackBoxQueryTypes;
import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.rewrite.RewriterException;
import uk.ac.ox.cs.pdq.rewrite.sql.SQLTranslator;
import uk.ac.ox.cs.pdq.rewrite.sql.SQLTranslator.SupportedDialect;

// TODO: Auto-generated Javadoc
/**
 * Blackbox cost estimator which translates a configuration directly to an SQL
 * query which is further provided to PostgreSQL for cost analysis.
 *
 * @author Efthymia Tsamoura
 * @param <P> the generic type
 */
public class PostgresqlBlackBoxEstimator<P extends Plan> implements BlackBoxCostEstimator<P> {

	/** The stats. */
	protected final StatisticsCollector stats;

	/**
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
	 * @param prop the prop
	 * @throws SQLException the SQL exception
	 */
	public PostgresqlBlackBoxEstimator(Properties prop) throws SQLException {
		this(null, prop, BlackBoxQueryTypes.DEFAULT);
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
		return (PostgresqlBlackBoxEstimator<P>) (this.stats == null ? new PostgresqlBlackBoxEstimator<>(null, this.properties, this.queryType) : new PostgresqlBlackBoxEstimator<>(this.stats.clone(), this.properties, this.queryType));
	}

	/**
	 * Gets the connection.
	 *
	 * @return a connection database connection for the given properties.
	 * @throws SQLException the SQL exception
	 */
	public Connection getConnection() throws SQLException {
		String url = this.properties.getProperty("url");
		String driver = this.properties.getProperty("driver");
		String database = this.properties.getProperty("database");
		String username = this.properties.getProperty("username");
		String password = this.properties.getProperty("password");
		return DriverManager.getConnection(url + database, username, password);
	}
	
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.cost.estimators.CostEstimator#estimateCost(uk.ac.ox.cs.pdq.util.Costable)
	 */
	@Override
	public DoubleCost cost(P plan) {
		DoubleCost result = estimateCost(plan);
		plan.setCost(result);
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.cost.estimators.CostEstimator#estimateCost(uk.ac.ox.cs.pdq.util.Costable)
	 */
	@Override
	public DoubleCost estimateCost(P plan) {
		double result = Double.POSITIVE_INFINITY;
		if(this.stats != null){this.stats.start(COST_ESTIMATION_TIME);}
		if (plan.isClosed()) {
			StringBuilder sql = new StringBuilder("EXPLAIN ");
			try {
				switch (this.queryType) {
				case SQL_WITH:
					sql.append(SQLTranslator.target(SupportedDialect.POSTGRESQL)
							.toSQLWith(plan.getEffectiveOperator()));
					break;
				default:
					sql.append(SQLTranslator.target(SupportedDialect.POSTGRESQL)
							.toSQL(plan.getEffectiveOperator()));
					break;
				}
			} catch (RewriterException e) {
				throw new IllegalStateException(e);
			}
			try(Connection conn = this.getConnection();
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
				throw new IllegalStateException(e.getMessage() + " for " + sql + "\n" + plan);
			}
		}
		if(this.stats != null){this.stats.stop(COST_ESTIMATION_TIME);}
		if(this.stats != null){this.stats.increase(COST_ESTIMATION_COUNT, 1);}
		return new DoubleCost(result);
	}
}

