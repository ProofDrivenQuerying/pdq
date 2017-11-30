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
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.BasicSelect;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.DatabaseUtilities;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;

/**
 * Single thread that represents a connection to a remote database provider.
 * 
 * @author Gabor
 *
 */
public class ExecutorThread extends Thread {

	/**
	 * Simple lock object
	 */
	private final Object RESULTS_LOCK = new Object();

	/**
	 * Flag to indicate when the current task is completed or failed.
	 */
	private boolean isFinished = false;
	/**
	 * A finished task has either results or resultException.
	 */
	private List<Match> results;
	/**
	 * When the database provider returns an exception insted of data.
	 */
	private Throwable resultException;

	/**
	 * The parent manager with the task queue.
	 */
	private ExecutionManager manager;
	/**
	 * The actual connection to the database provider.
	 */
	private Connection connection;
	/**
	 * shutdown is requested
	 */
	private boolean shutdown = false;

	/**
	 * Database driver types. This is necessary to know what flavour of SQL the
	 * database provider speaks.
	 * 
	 * @author Gabor
	 *
	 */
	public enum DriverType {
		Derby, MySql, Postgres
	};

	/**
	 * The SQL flavour type of this connection.
	 */
	private DriverType driverType;
	/**
	 * Database name.
	 */
	private String databaseName = null;

	/**
	 * Creates a connection to the database provider, and creates a PDQ_WORK schema
	 * or database in it.
	 * 
	 * @param databaseParameters
	 * @param manager
	 * @throws DatabaseException
	 */
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

	/**
	 * Limits the max lengh of the database name, makes sure it is all uppercase.
	 * 
	 * @param databaseIn
	 * @return
	 */
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

	/*
	 * Main thread function. checks for tasks, and executes the next one when there
	 * is one. After execution it waits for the results to be read by the requestor.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		while (!shutdown) {
			// wait for next task:
			Task task = waitForNextTask();
			if (shutdown || task == null) {
				continue;
			}
			// register that this thread started working on it
			synchronized (task) {
				// we got the task, so let's take ownership.
				task.setExecutorThread(this);
				// let the executor know we started processing this request.
				task.notify();
			}

			// execute task
			try {
				results = execute(task.getCommand());
			} catch (Throwable t) {
				resultException = t;
			}

			// register that task is finished, notify those waiting for the result.
			synchronized (RESULTS_LOCK) {
				isFinished = true;
				// wake up the one waiting for the results.
				RESULTS_LOCK.notify();
			}

			// wait for results to be read, so we can start monitoring new tasks.
			waitForResultsRead();
		}

		// thread is ending, close the connection.
		closeConnection();
	}

	/**
	 * Waits until this thread is clean and results were read.
	 */
	private void waitForResultsRead() {
		// local copy of the flag isFinished.
		boolean isFinishedTmp = true;
		// wait for results to be read, so we can start monitoring new tasks.
		// in case of shutdown we will stop waiting.
		while (isFinishedTmp && !shutdown) {
			synchronized (RESULTS_LOCK) {
				isFinishedTmp = this.isFinished;
				try {
					if (isFinishedTmp)
						RESULTS_LOCK.wait(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Waits until we get a new task.
	 * 
	 * @return
	 */
	private Task waitForNextTask() {
		Task task = null;
		while (task == null && !shutdown) {
			synchronized (manager.TASKS_LOCK) {
				// wait a bit, or in case of notified get the next task
				try {
					manager.TASKS_LOCK.wait(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				task = manager.getTasks().poll();
			}
		}
		return task;
	}

	/**
	 * Execute a single command.
	 */
	private List<Match> execute(Command command) throws DatabaseException, SQLException {
		List<String> statements = null;

		// convert the command into the corresponding SQL dialect.
		switch (driverType) {
		case Derby:
			statements = command.toDerbyStatement(databaseName);
			break;
		case MySql:
			statements = command.toMySqlStatement(databaseName);
			break;
		case Postgres:
			statements = command.toPostgresStatement(databaseName);
			break;
		}

		// Selects have to be executed by the JDBC interface's executeQuery function,
		// while inserts and other SQL commands have to be executed by calling the
		// executeUpdate function. Everything that extends BasicSelect will be executed
		// as a query, one by one. Updates can be batch executed.
		if (command instanceof BasicSelect) {
			List<Match> results = new ArrayList<>();
			for (String statement : statements) {
				// queries one by one.
				results.addAll(executeQuery(statement, (BasicSelect) command));
			}
			return results;
		} else {
			// batch update.
			executeUpdate(statements);
			return new ArrayList<>();
		}
	}

	/**
	 * Batch execution of update commands. Everything that does not return a
	 * resultSet is an update.
	 * 
	 * @param statements
	 *            - Commands that are not related to BasicSelect
	 * @return
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	private void executeUpdate(List<String> statements) throws DatabaseException, SQLException {
		Statement sqlStmt = null;
		try {
			sqlStmt = connection.createStatement();
			for (String s : statements)
				sqlStmt.addBatch(s);
			sqlStmt.executeBatch();
		} catch (SQLException e) {
			if (e.getNextException() != null)
				throw new DatabaseException("Error while executing update: " + e.getMessage() + " - " + statements, e.getNextException());
			throw new DatabaseException("Error while executing update: " + statements, e);
		} catch (Throwable t) {
			throw new DatabaseException("Error while executing update: " + statements, t);
		} finally {
			if (sqlStmt != null)
				sqlStmt.close();
		}
	}

	/**
	 * Executes a single query statement.
	 * 
	 * @param statements
	 *            the query statement
	 * @param command
	 *            the original request command - needed to parse the resultSet and
	 *            create Match objects.
	 * @return List of Matches, each result record will be wrapped in a Match
	 *         object.
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	private List<Match> executeQuery(String statements, BasicSelect command) throws DatabaseException, SQLException {
		List<Match> results = new ArrayList<>();
		ResultSet resultSet = null;
		Statement sqlStmt = null;
		try {
			sqlStmt = connection.createStatement();
			// execute the query
			resultSet = sqlStmt.executeQuery(statements);
		} catch (Throwable t) {
			throw new DatabaseException("Error while executing query: " + statements, t);
		}
		try {
			// parse results
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
				// create the match object for this record (fact)
				results.add(Match.create(command.getFormula(), map));
			}
		} finally {
			if (resultSet != null)
				resultSet.close();
			if (sqlStmt != null)
				sqlStmt.close();
		}
		return results;
	}

	/**
	 * Will immediately terminate this thread if it is in a waiting state, but will
	 * finish current execution before shutting down.
	 * 
	 * @throws DatabaseException
	 *             in case of timeout while shutting down.
	 */
	public void shutdown() throws DatabaseException {
		shutdown = true;
		synchronized (manager.TASKS_LOCK) {
			// wake up every thread waiting for new tasks.
			manager.TASKS_LOCK.notifyAll();
		}
		synchronized (RESULTS_LOCK) {
			// wake this thread up in case it is waiting for the results to be read.
			RESULTS_LOCK.notify();
		}
		// wait for the thread to stop.
		int timeoutCounter = 0;
		while (connection != null) {
			timeoutCounter++;
			if (timeoutCounter > 200) {
				throw new DatabaseException("Database connection thread won't shut down!");
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Closes the underlying SQL connection.
	 */
	private void closeConnection() {
		try {
			connection.close();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		connection = null;
	}

	/**
	 * Returns the results from the latest execution. Throws exception in case the
	 * last execution was causing an exception.
	 * 
	 * Resets the thread state, so it can start working on the next task.
	 * 
	 * @return
	 * @throws Throwable
	 */
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
