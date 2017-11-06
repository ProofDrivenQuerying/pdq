package uk.ac.ox.cs.pdq.data.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.DatabaseUtilities;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;

/**
 * Represents a configurable amount of connections to external database servers
 * such as Derby, MySql or Postgres. Can execute an SQLQuery or an SQLUpdate
 * using single thread or multiple threads.
 * 
 * @author Gabor
 *
 */
public class SQLDatabaseConnection {
	private int synchronousThreadsNumber = 10;
	/** Open database connections. */
	private List<Connection> synchronousConnections = Lists.newArrayList();
	private boolean isDerby;
	private DatabaseParameters databaseParameters;

	/** Initialises configured amount of connections to the configured database.
	 * @param parameters
	 * @throws SQLException 
	 */
	protected SQLDatabaseConnection(DatabaseParameters databaseParameters) throws SQLException {
		this.databaseParameters = databaseParameters;
		String threadNumber = databaseParameters.getProperty("synchronousThreadsNumber");
		if (threadNumber != null && !threadNumber.isEmpty()) {
			try {
				synchronousThreadsNumber = Integer.parseInt(threadNumber);
				System.out.println("SQL - synchronousThreadsNumber configured to "+ synchronousThreadsNumber);
			} catch(Exception e) {
				System.err.println("SQL - synchronousThreadsNumber failed to parse. " + threadNumber);
			}
		}
		String driver = databaseParameters.getDatabaseDriver();
		String url = databaseParameters.getConnectionUrl();
		String database = databaseParameters.getDatabaseName();
		String username = databaseParameters.getDatabaseUser();
		String password = databaseParameters.getDatabasePassword();
		if (driver.contains("derby")) {
			isDerby = true;
			database = validateDatabaseName(database);
			databaseParameters.setDatabaseName(database);
			username = "APP_" + GlobalCounterProvider.getNext("DatabaseConnectionName");
			password = "";
		}

		for (int j = 0; j < synchronousThreadsNumber; j++)
			this.synchronousConnections.add(DatabaseUtilities.getConnection(driver, url, database, username, password));
		if (!driver.contains("derby") && !database.contains("_work")) {
			// In case of postgres and mysql we need to have 2 databases, one we used to connect to, and a secondary to use.
			database = database+"_work";
			databaseParameters.setDatabaseName(database);
		}
		if (isDerby) {
			Statement st = this.synchronousConnections.get(0).createStatement();
			st.execute("create schema " + database);
		}
	}
	private String validateDatabaseName(String databaseIn) {
		String newDatabaseName = databaseIn;
		if (Strings.isNullOrEmpty(newDatabaseName)) {
			newDatabaseName = "chase";
		}
		// database name cannot be longer then 128 character, so if it is close we shorten it, 
		// add current time to make sure it is unique even in case of multiple runs.
		if (newDatabaseName.length() > 90) {
			newDatabaseName = newDatabaseName.substring(0, 80) + "__";
		}
		newDatabaseName += "_" + System.currentTimeMillis() + "_" + GlobalCounterProvider.getNext("DatabaseConnectionName");
		
		newDatabaseName = newDatabaseName.toUpperCase();
		return newDatabaseName;
	}

	/**
	 * Closes all connections.
	 */
	protected void close() {
		if (synchronousConnections.isEmpty()) 
			return; // already closed.
		for (Connection connection : this.synchronousConnections) {
			try {
				connection.close();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		synchronousConnections.clear();
	}
	
	/** Executes a query. Uses pre-configured amount of threads.
	 * @param query
	 * @return
	 * @throws SQLException 
	 * @throws DatabaseException 
	 */
	protected List<Match> executeQuery(SQLQuery query) throws SQLException, DatabaseException {
		List<Match> results = new ArrayList<>();
			Statement sqlStatement = this.synchronousConnections.get(0).createStatement();
			Formula source = query.getFormula();
			String sQuery = query.convertToSqlQueryString(databaseParameters.getDatabaseName());
			ResultSet resultSet = null;
			try {
				resultSet = sqlStatement.executeQuery(sQuery);
			}catch(Throwable t) {
				throw new DatabaseException("Error while executing query: " + sQuery,t);
			}
			try {
				while (resultSet.next()) {
					int f = 1;
					Map<Variable, Constant> map = new LinkedHashMap<>();
					for(Term term:source.getTerms()) {
						if (term instanceof Variable) {
							Variable variable = (Variable) term;
							String assigned = resultSet.getString(f);
							TypedConstant constant = null;
							if (assigned!= null && assigned.startsWith("_Typed")) {
								constant = TypedConstant.deSerializeTypedConstant(assigned); 
							} 
							Constant constantTerm = constant != null ? constant : UntypedConstant.create(assigned);
							map.put(variable, constantTerm);
							f++;
						}
					}
					results.add(Match.create(source,map));
				}
			}finally {
				if (resultSet!=null)
					resultSet.close();
			}
		return results;
	}
	
	/** Executes an update and returns the number of affected rows. Uses pre-configured amount of threads.
	 * @param update
	 * @return
	 */
	protected int executeUpdate(SQLUpdate update) {
		return 0;
	}
	
	protected boolean isDerby() {
		return isDerby;
	}
	
	protected int[] executeStatements(Collection<String> statements) throws SQLException {
		Statement st = null;
		try {
			st = synchronousConnections.get(0).createStatement();
			for (String statement:statements) {
				st.addBatch(statement);
			}
			return st.executeBatch();
		}catch(SQLException e) {
			System.err.println("Error while executing batch commands: " + statements);
			if (st != null) {
				try {
					System.err.println("SQL warnings: " + st.getWarnings());
				} catch (SQLException e2) {
					e2.printStackTrace();
				}
			}
			throw e;
		}
	}
}
