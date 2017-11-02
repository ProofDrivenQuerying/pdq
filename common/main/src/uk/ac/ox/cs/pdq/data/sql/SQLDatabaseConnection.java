package uk.ac.ox.cs.pdq.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.DatabaseUtilities;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Constant;
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
	protected List<Connection> synchronousConnections = Lists.newArrayList();
	private boolean isDerby;

	/** Initialises configured amount of connections to the configured database.
	 * @param parameters
	 * @throws SQLException 
	 */
	protected SQLDatabaseConnection(DatabaseParameters databaseParameters) throws SQLException {
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
	 */
	protected List<Match> executeQuery(SQLQuery query) throws SQLException {
		List<Match> ret = new ArrayList<>();
		PreparedStatement st = synchronousConnections.get(0).prepareStatement("select * from r1");
		ResultSet rs = st.executeQuery();
		while (rs.next()) {
			Map<Variable, Constant> map = new HashMap<>();
			for (int column=0; column < query.getFormula().getTerms().length; column++) {
				String value = rs.getString(column);
				map.put((Variable)query.getFormula().getTerms()[column], TypedConstant.create(value));
			}
			ret.add(Match.create(query.getFormula(),map)); 		
		}
		return ret;
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
}
