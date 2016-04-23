package uk.ac.ox.cs.pdq.workloadgen.database;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.cost.statistics.Histogram;
import uk.ac.ox.cs.pdq.workloadgen.query.Query;
import uk.ac.ox.cs.pdq.workloadgen.query.View;
import uk.ac.ox.cs.pdq.workloadgen.schema.Attribute;
import uk.ac.ox.cs.pdq.workloadgen.schema.Table;

import com.google.common.base.Strings;

/**
 *
 * @author Efthymia Tsamoura
 *
 */
public abstract class DatabaseManager implements AutoCloseable{

	/** Logger. */
	private static Logger log = Logger.getLogger(DatabaseManager.class);

	/** Connection to the database */
	protected Connection connection;

	/** Chase database name */
	protected final String database;
	/** Information to connect to the facts database*/
	protected final String driver;
	protected final String url;
	protected final String username;
	protected final String password;

	/**
	 * 
	 * @param driver
	 * 		Database driver
	 * @param url
	 * 		Database url
	 * @param database
	 * 		Database name
	 * @param username
	 * 		Database user
	 * @param password
	 * 		Database pass
	 * @param builder
	 * 		Builds SQL queries that detect homomorphisms
	 * @param schema
	 * 		Input schema
	 * @param query
	 * 		Input query
	 * @throws SQLException
	 */
	public DatabaseManager(
			String driver, 
			String url, 
			String database,
			String username, 
			String password
			) throws SQLException {
		this.connection = getConnection(driver, url, database, username, password);
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
		this.database = database;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() throws Exception {
		this.connection.close();
	}

	/**
	 * 
	 * @param query
	 * @return the size of the input query
	 */
	public Integer getSize(Query query) {
		String statement = "SELECT COUNT(*) AS size\nFROM" + "(\n" + query.toString() +"\n)AS T;" ;

		try(Statement sqlStatement = connection.createStatement();
				ResultSet resultSet = sqlStatement.executeQuery(statement)) {
			int rows = 0;
			while (resultSet.next()) {
				rows = resultSet.getInt("size");
			}
			return rows;
		} catch (SQLException ex) {
			log.debug(query);
			throw new IllegalStateException(ex.getMessage(), ex);
		}
	}

	/**
	 * 
	 * @param schemaName
	 * @param table
	 * @return
	 * 		the DBMS statistics for the input table
	 */
	public abstract Map<Attribute, String> getStatistics(String schemaName, Table table);

	/**
	 * 
	 * @param schemaName
	 * @param view
	 * @return
	 * 		the DBMS statistics for the input view
	 */
	public abstract Map<Attribute, String> getStatistics(String schemaName, View view);

	/**
	 * 
	 * @param schemaName
	 * @param table
	 * @param statistic
	 * @return
	 * 		the histogram associated with the input table statistic
	 */
	public abstract Histogram getHistogram(String schemaName, Table table, Attribute attribute, String statistic);

	/**
	 * 
	 * @param schemaName
	 * @param view
	 * @param statistic
	 * @return
	 * 		the histogram associated with the input view statistic
	 */
	public abstract Histogram getHistogram(String schemaName, View view, Attribute attribute, String statistic);

	/**
	 * 
	 * @param query
	 * @return the size of the input query as estimated by the DBMS cardinality estimation module
	 */
	public BigInteger getSizeEstimate(Query query) {
		return null;
	}

	/**
	 * Creates the database view
	 * @param view
	 */
	public abstract void createView(String schemaName, View view);

	/**
	 * Drops the database view
	 * @param view
	 */
	public abstract void dropView(View view);	
	
	/**
	 * Creates statistics for the input view
	 * @param view
	 */
	public abstract String createStatistics(String schemaName, View view, List<Attribute> attributes);

	/**
	 * @param url
	 * @param database
	 * @param username
	 * @param password
	 * @param driver String
	 * @return a connection database connection for the given properties.
	 * @throws SQLException
	 */
	public static Connection getConnection(String driver, String url, String database, String username, String password) throws SQLException {
		if (!Strings.isNullOrEmpty(driver)) {
			try {
				Class.forName(driver).newInstance();
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException("Could not load chase database driver '" + driver + "'");
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String u = null;
		if (url.contains("{1}")) {
			u = url.replace("{1}", database);
		} else {
			u = url + database;
		}
		try {
			Connection result = DriverManager.getConnection(u, username, password);
			result.setAutoCommit(true);
			return result;
		} catch (SQLException e) {
			log.debug(e.getMessage());
		}
		Connection result = DriverManager.getConnection(url, username, password);
		result.setAutoCommit(true);
		return result;
	}

}
