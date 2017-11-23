package uk.ac.ox.cs.pdq.databasemanagement.execution;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;

import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.Command;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.Query;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.DatabaseUtilities;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;

public class ExecutorThread extends Thread {

	public final Object RESULTS_LOCK = new Object();
	// private DatabaseParameters parameters;
	protected boolean isFinished = false;
	private List<Match> results;
	private ExecutionManager manager;
	private Connection connection;
	private boolean shutdown = false;
	private Throwable resultException;
	
	public enum DriverType {
		Derby, MySql, Postgres
	};

	private DriverType driverType;
	private String databaseName = null;

	public ExecutorThread(DatabaseParameters databaseParameters, ExecutionManager manager) throws DatabaseException {
		// this.parameters = databaseParameters;
		this.manager = manager;
		String driver = databaseParameters.getDatabaseDriver();
		String url = databaseParameters.getConnectionUrl();
		String database = databaseParameters.getDatabaseName();
		String username = databaseParameters.getDatabaseUser();
		String password = databaseParameters.getDatabasePassword();
		if (driver.contains("derby")) {
			driverType = DriverType.Derby;
			database = validateDatabaseName(database);
			databaseParameters.setDatabaseName(database);
			username = "APP_" + GlobalCounterProvider.getNext("DatabaseConnectionName");
			password = "";
		}
		if (driver.toLowerCase().contains("mysql")) {
			driverType = DriverType.MySql;
		}

		if (driver.toLowerCase().contains("postgres")) {
			driverType = DriverType.Postgres;
		}
		if (driverType == null)
			throw new DatabaseException("Invalid driver type!" + driver);
		try {
			String dbToConnect = database;
			if (!driver.contains("derby") && database.contains("_work")) {
				dbToConnect = database.substring(0, database.indexOf("_work"));
			}
			connection = DatabaseUtilities.getConnection(driver, url, dbToConnect, username, password);
		} catch (SQLException e) {
			throw new DatabaseException("Connection failed to url: " + url + " using database: " + database + ", driver: " + driver, e);
		}
		if (!driver.contains("derby") && !database.contains("_work")) {
			// In case of postgres and mysql we need to have 2 databases, one we used to
			// connect to, and a secondary to use.
			database = database + "_work";
			databaseParameters.setDatabaseName(database);
		}
		this.databaseName = database;
		try {
			if (driver.contains("derby")) {
				Statement st = connection.createStatement();
				st.execute("create schema " + database);
			}
		} catch (SQLException e) {
			throw new DatabaseException("Derby database-schema creation failed: " + database, e);
		}

	}

	private String validateDatabaseName(String databaseIn) {
		String newDatabaseName = databaseIn;
		if (Strings.isNullOrEmpty(newDatabaseName)) {
			newDatabaseName = "chase";
		}
		// database name cannot be longer then 128 character, so if it is close we
		// shorten it,
		// add current time to make sure it is unique even in case of multiple runs.
		if (newDatabaseName.length() > 90) {
			newDatabaseName = newDatabaseName.substring(0, 80) + "__";
		}
		newDatabaseName += "_" + System.currentTimeMillis() + "_" + GlobalCounterProvider.getNext("DatabaseConnectionName");

		newDatabaseName = newDatabaseName.toUpperCase();
		return newDatabaseName;
	}

	public void run() {
		while (!shutdown) {
			Task task = null;
			synchronized (manager.TASKS_LOCK) {
				// wait a bit, or in case of notified get the next task
				try {
					manager.TASKS_LOCK.wait(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				task = manager.getTasks().poll();
			}
			if (task == null) {
				// if we got no task go back and wait a bit.
				continue;
			}
			synchronized (task) {
				// we got the task, so let's take ownership.
				task.setExecutorThread(this);
				// let the executor know we started processing this request.
				task.notify();
			}
			try {
				results = execute(task.getCommand());
			} catch (Throwable t) {
				resultException = t;
			}
			synchronized (RESULTS_LOCK) {
				isFinished = true;
				// wake up the one waiting for the results.
				RESULTS_LOCK.notify();
			}
			boolean isFinished = true;
			// wait for results to be read, so we can start monitoring new tasks.
			while (isFinished) {
				synchronized (RESULTS_LOCK) {
					isFinished = this.isFinished;
					try {
						if (isFinished)
							RESULTS_LOCK.wait(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		closeConnection();
	}

	private List<Match> execute(Command command) throws DatabaseException, SQLException {
		List<String> statements = null;
		switch (driverType) {
		case Derby:
			statements = command.toDerbyStatement(databaseName, null);
			break;
		case MySql:
			statements = command.toMySqlStatement(databaseName, null);
			break;
		case Postgres:
			statements = command.toPostgresStatement(databaseName, null);
			break;
		}
		System.out.println("Executing " + statements);
		if (command instanceof Query) {
			List<Match> results = new ArrayList<>();
			for (String statement:statements) {
				results.addAll(executeQuery(statement, (Query)command));
			}
			return results;
		} else {
			return executeUpdate(statements, command);
		}
	}
	private List<Match> executeUpdate(List<String> statements, Command command) throws DatabaseException, SQLException {
		Statement sqlStmt = null;
		try {
			sqlStmt = connection.createStatement();
			for (String s:statements) sqlStmt.addBatch(s);
			sqlStmt.executeBatch();
			return new ArrayList<>(); 
		} catch (SQLException e) {
			if (e.getNextException()!=null)
				throw new DatabaseException("Error while executing update: " + statements, e.getNextException());
			throw new DatabaseException("Error while executing update: " + statements, e);
		} catch (Throwable t) {
			throw new DatabaseException("Error while executing update: " + statements, t);
		} finally {
			if (sqlStmt!=null)
				sqlStmt.close();
		}
	}

	private List<Match> executeQuery(String statements, Query command) throws DatabaseException, SQLException {
		List<Match> results = new ArrayList<>(); 
		ResultSet resultSet = null;
		try {
			resultSet = connection.createStatement().executeQuery(statements);
		} catch (Throwable t) {
			throw new DatabaseException("Error while executing query: " + statements, t);
		}
		try {
			while (resultSet.next()) {
				int f = 1;
				Map<Variable, Constant> map = new LinkedHashMap<>();
				for (Term term : command.getResultTerms()) {
					if (term instanceof Variable) {
						Variable variable = (Variable) term;
						String assigned = resultSet.getString(f);
						TypedConstant constant = null;
						if (assigned != null && assigned.startsWith("_Typed")) {
							constant = TypedConstant.deSerializeTypedConstant(assigned);
						}
						Constant constantTerm = constant != null ? constant : UntypedConstant.create(assigned);
						map.put(variable, constantTerm);
						f++;
					}
				}
				results.add(Match.create(command.getFormula(), map));
			}
		} finally {
			if (resultSet != null)
				resultSet.close();
		}	
		return results;
	}

	/**
	 * Will immediately terminate this thread if it is in a waiting state, but will
	 * finish current execution before shutting down.
	 */
	public void shutdown() {
		shutdown = true;
		synchronized (manager.TASKS_LOCK) {
			// wake up the thread
			manager.TASKS_LOCK.notify();
		}
	}

	private void closeConnection() {
		try {
			connection.close();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public List<Match> getResultsAndReset() throws Throwable {
		boolean isFinished = false;
		// wait for results to be ready.
		while (!isFinished) {
			synchronized (RESULTS_LOCK) {
				isFinished = this.isFinished;
				try {
					if (!isFinished)
						RESULTS_LOCK.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
		// give results and reset status.
		synchronized (RESULTS_LOCK) {
			if (resultException != null)
				throw resultException;
			this.isFinished = false;
			List<Match> ret = this.results;
			this.results = null;
			RESULTS_LOCK.notify(); // wake up the thread and continue executing tasks.
			return ret;
		}
	}

}
