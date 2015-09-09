package uk.ac.ox.cs.pdq.runtime.query;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Signature;
import uk.ac.ox.cs.pdq.rewrite.RewriterException;
import uk.ac.ox.cs.pdq.rewrite.sql.SQLTranslator;
import uk.ac.ox.cs.pdq.runtime.EvaluationException;
import uk.ac.ox.cs.pdq.util.BooleanResult;
import uk.ac.ox.cs.pdq.util.Result;
import uk.ac.ox.cs.pdq.util.Table;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.Typed;
import uk.ac.ox.cs.pdq.util.Types;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

/**
 * Generic SQL evaluator. Builds SQL statements from conjunctive queries, and
 * runs them on some underlying database.
 * 
 * @author Julien Leblay
 */
public class SQLQueryEvaluator implements QueryEvaluator {

	/** The logger */
	public static Logger log = Logger.getLogger(SQLQueryEvaluator.class);
	
	/** The database connection */
	private final Connection connection;
	
	/** The query to be evaluated */
	private final Query<?> query;

	/** The evaluator's event bus */
	private EventBus eventBus;
	
	/**
	 * Default construction
	 * @param conn the database connection to use for the query evaluation.
	 */
	private SQLQueryEvaluator(Connection connection)  throws SQLException {
		this(connection, null);
	}
	
	/**
	 * Default construction
	 * @param conn the database connection to use for the query evaluation.
	 * @param query Query
	 */
	private SQLQueryEvaluator(Connection connection, Query<?> query)  throws SQLException {
		this.connection = connection;
		this.query = query;
	}

	/**
	 * @param eventBus EventBus
	 * @see uk.ac.ox.cs.pdq.runtime.query.QueryEvaluator#setEventBus(EventBus)
	 */
	@Override
	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	/**
	 * @param connection Connection
	 * @return SQLQueryEvaluator
	 */
	public static SQLQueryEvaluator newEvaluator(Connection connection) throws SQLException {
		return new SQLQueryEvaluator(connection);
	}

	/**
	 * @param connection Connection
	 * @param query Query
	 * @return SQLQueryEvaluator
	 */
	public static SQLQueryEvaluator newEvaluator(Query<?> query) throws SQLException, EvaluationException {
		Properties prop = findRelationalProperties(query);
		if (prop == null) {
			throw new EvaluationException("Unable to evaluate query over a SQL database.");
		}
		return new SQLQueryEvaluator(getConnection(prop), query);
	}
	
	/**
	 * Evalue the given SQL query and return the result into a table.
	 * @param sql
	 * @param output List<Typed>
	 * @return a table containing the result of the query
	 * @throws EvaluationException
	 */
	public Table evaluate(String sql, List<Typed> output) throws EvaluationException {
		try(Statement stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql)) {
			
			Table result = new Table(output);
			while (rs.next()) {
				Object[] ndata = new Object[result.columns()];
				for (int index = 0; index < ndata.length; ++index) {
					Type columnType = result.getType().getType(index);
					if (columnType == Integer.class) {
						ndata[index] = new Integer(rs.getInt(index + 1));

					} else if (columnType == String.class) {
						ndata[index] = rs.getString(index + 1).trim();

					} else {
						Method m = ResultSet.class.getMethod("get" + Types.simpleName(columnType), int.class);
						ndata[index] = m.invoke(rs, index + 1);
					}
				}
				Tuple t = result.getType().createTuple(ndata);
				result.appendRow(t);
				if (this.eventBus != null) {
					this.eventBus.post(t);
				}
			}
			return result;
		} catch (SQLException 
				| NoSuchMethodException 
				| IllegalAccessException
				| InvocationTargetException e) {
			throw new EvaluationException(e.getMessage(), e);
		}
	}

	/**
	 * @return Result
	 * @throws EvaluationException
	 * @see uk.ac.ox.cs.pdq.runtime.query.QueryEvaluator#evaluate()
	 */
	@Override
	public Result evaluate() throws EvaluationException {
		Preconditions.checkState(this.query instanceof ConjunctiveQuery, "Non-conjunctive queries not yet supported.");
		Preconditions.checkNotNull(this.query, "No query defined for evaluation.");

		ConjunctiveQuery q = (ConjunctiveQuery) this.query;
		String sql;
		try {
			sql = SQLTranslator.generic().toSQL(q);
		} catch (RewriterException e) {
			throw new EvaluationException(e.getMessage(), e);
		}

		Table result = new Table(Utility.termsToAttributes(q));
		try {
			if (this.connection.isClosed()) {
				throw new EvaluationException("Attempting to evalute query on a closed connection.");
			}
		} catch (SQLException e) {
			log.warn(sql, e);
			throw new EvaluationException(sql, e);
		}
		
		log.debug("Evaluating query : " + sql);
		try(Statement stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql)) {
			if (q.getFree().isEmpty()) {
				if (rs.next()) {
					return new BooleanResult(true);
				}
				return new BooleanResult(false);
			}
			while (rs.next()) {
				Object[] ndata = new Object[result.columns()];
				for (int index = 0; index < ndata.length; ++index) {
					Type columnType = result.getType().getType(index);
					if (columnType == Integer.class) {
						ndata[index] = new Integer(rs.getInt(index + 1));

					} else if (columnType == String.class) {
						ndata[index] = rs.getString(index + 1).trim();

					} else if (columnType == BigDecimal.class) {
						ndata[index] = rs.getBigDecimal(index + 1);

					} else {
						Method m = ResultSet.class.getMethod(
								"get" + Types.simpleName(columnType),
								int.class);
						ndata[index] = m.invoke(rs, index + 1);
					}
				}
				Tuple t = result.getType().createTuple(ndata);
				result.appendRow(t);
				if (this.eventBus != null) {
					this.eventBus.post(t);
				}
			}
		} catch (SQLException | ReflectiveOperationException e) {
			log.warn(e.getMessage());
			log.warn(sql, e);
			throw new EvaluationException(sql, e);
		}
		return result;
	}

	/**
	 * @param properties
	 * @return a connection database connection for the given properties.
	 * @throws SQLException
	 */
	public static Connection getConnection(Properties properties) throws SQLException {
		String url = properties.getProperty("url");
		String database = properties.getProperty("database");
		String username = properties.getProperty("username");
		String password = properties.getProperty("password");
		return DriverManager.getConnection(url + database, username, password);
	}

	/**
	 * @param plan Plan
	 * @return the schema underlying relational's properties if all of the
	 * relations in the schema have the same properties, null otherwise.
	 */
	private static Properties findRelationalProperties(Query<?> query) {
		Properties result = null;
		for (Predicate pred: query.getPredicates()) {
			Signature sig = pred.getSignature();
			Properties properties = new Properties();
			if (sig instanceof Relation) {
				properties.putAll(((Relation) sig).getProperties());
				properties.remove(Relation.PropertyKeys.METADATA);
				if (result == null) {
					result = properties;
				} else if (!(properties.equals(result))) {
					return properties;
				}
			}
		}
		return result;
	}
}
