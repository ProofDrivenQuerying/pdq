package uk.ac.ox.cs.pdq.reasoningdatabase.execution;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;

import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoningdatabase.DataSink;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseParameters;
import uk.ac.ox.cs.pdq.reasoningdatabase.sqlcommands.BasicSelect;
import uk.ac.ox.cs.pdq.reasoningdatabase.sqlcommands.Command;
import uk.ac.ox.cs.pdq.reasoningdatabase.sqlcommands.DropDatabase;
import uk.ac.ox.cs.pdq.reasoningdatabase.sqlcommands.InsertSelect;
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
		MySql, Postgres
	};

	/**
	 * The SQL flavour type of this connection.
	 */
	private DriverType driverType;
	/**
	 * Database name.
	 */
	private String databaseName = null;
	private boolean allowDatabaseDropping = false;
	/**
	 * Extra error logging when set to true.
	 */
	boolean debug = true;
	/**
	 * Creates a connection to the database provider, and creates a PDQ_WORK schema
	 * or database in it.
	 * 
	 * @param databaseParameters
	 * @param manager
	 * @throws DatabaseException
	 */
	public ExecutorThread(DatabaseParameters databaseParameters, ExecutionManager manager) throws DatabaseException {
		super("Executor" + getDriverType(databaseParameters.getDatabaseDriver()) + "_"+GlobalCounterProvider.getNext("ExecutorThread"));
		this.manager = manager;
		String driver = databaseParameters.getDatabaseDriver();
		if (driver!=null && driver.toLowerCase().contains("mysql")) {
			// MySql is not supported any more, switching to default postgres instead.
			databaseParameters = DatabaseParameters.Postgres;
			driver = databaseParameters.getDatabaseDriver();
		}
		if (databaseParameters.isCreateNewDatabase()) {
			allowDatabaseDropping = true; // freshly created database can be dropped.
		}
		String url = databaseParameters.getConnectionUrl();
		String database = databaseParameters.getDatabaseName();
		String username = databaseParameters.getDatabaseUser();
		String password = databaseParameters.getDatabasePassword();
		driverType = getDriverType(driver);
		try {
			String dbToConnect = database;
			if (database.contains("_WORK")) {
				dbToConnect = database.substring(0, database.indexOf("_WORK"));
			}
			connection = getConnection(driver, url, dbToConnect, username, password);
		} catch (SQLException e) {
			throw new DatabaseException("Connection failed to url: " + url + " using database: " + database + ", driver: " + driver, e);
		}
		if (!database.contains("_WORK") && databaseParameters.isCreateNewDatabase()) {
			// we need to have 2 databases, one we used to connect to, and a secondary to use.
			database = database + "_WORK";
			databaseParameters.setDatabaseName(database);
		}
		this.databaseName = database;
		try {
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// ignored error.
			e.printStackTrace();
		}
	}

	private static DriverType getDriverType(String driver) throws DatabaseException {
		
		DriverType driverType = null;
		if (driver==null || driver.isEmpty())
			throw new DatabaseException("Driver must be set to \"mysql\" or \"postgres\". Currently it is not set.");
		if (driver.toLowerCase().contains("mysql")) {
			driverType = DriverType.MySql;
		} else if (driver.toLowerCase().contains("postgres")) {
			driverType = DriverType.Postgres;
		} else {
			throw new DatabaseException("Unknown Driver: " + driver);
		}
		return driverType;
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
				if (task.isGeneric()) {
					task.setGenericResults(executeGeneric(task.getCommand()),null);
				} else {
					task.setResults(execute(task.getCommand()),null);
				}
			} catch (Throwable t) {
				task.setResults(null,t);
				System.err.println("Execution error while executing: " + task.getCommand());
				task.getCommand().printCallerStackTrace();
				//t.printStackTrace();
			}
		}

		// thread is ending, close the connection.
		closeConnection();
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
		case MySql:
			statements = command.toMySqlStatement(databaseName);
			break;
		case Postgres:
			if (allowDatabaseDropping) {
				statements = command.toPostgresStatement(databaseName);
			} else {
				List<String> statementsTmp = command.toPostgresStatement("ToDelete");
				statements = new ArrayList<>();
				for (String statement:statementsTmp) {
					statements.add(statement.replaceAll("ToDelete.",""));
				}
			}
			break;
		}
		boolean ignoreErrors = command.isIgnoreErrors();
		// Selects have to be executed by the JDBC interface's executeQuery function,
		// while inserts and other SQL commands have to be executed by calling the
		// executeUpdate function. Everything that extends BasicSelect will be executed
		// as a query, one by one. Updates can be batch executed.
		
		if (command instanceof BasicSelect) {
			List<Match> results = new ArrayList<>();
			for (String statement : statements) {
				// queries one by one.
				try {
					if (((BasicSelect) command).getSink()!=null) {
						executeQuery(statement, (BasicSelect) command, ((BasicSelect) command).getSink());
					} else {
						results.addAll(executeQuery(statement, (BasicSelect) command));
					}
				} catch (Throwable t) {
					if (ignoreErrors)
						t.printStackTrace();
					else
						throw t;
				}
			}
			return results;
		} else {
			// batch update.
			executeUpdate(statements, ignoreErrors);
			return new ArrayList<>();
		}
	}
	private List<String> executeGeneric(Command command) throws DatabaseException, SQLException {
		List<String> statements = null;
		if (command instanceof DropDatabase && !allowDatabaseDropping) {
			throw new DatabaseException("A database can only be dropped if it was created by this connection.");
		}
		// convert the command into the corresponding SQL dialect.
		switch (driverType) {
		case MySql:
			statements = command.toMySqlStatement(databaseName);
			break;
		case Postgres:
			if (allowDatabaseDropping) {
				statements = command.toPostgresStatement(databaseName);
			} else {
				List<String> statementsTmp = command.toPostgresStatement("ToDelete");
				statements = new ArrayList<>();
				for (String statement:statementsTmp) {
					statements.add(statement.replaceAll("ToDelete.",""));
				}
			}
			break;
		}
		boolean ignoreErrors = command.isIgnoreErrors();
		// Selects have to be executed by the JDBC interface's executeQuery function,
		// while inserts and other SQL commands have to be executed by calling the
		// executeUpdate function. Everything that extends BasicSelect will be executed
		// as a query, one by one. Updates can be batch executed.
		if (command instanceof InsertSelect) {
			executeUpdate(statements, ignoreErrors);
			return new ArrayList<>();
		} else if (command instanceof BasicSelect || command.toPostgresStatement(databaseName).get(0).toLowerCase().startsWith("select ")) {
			List<String> results = new ArrayList<>();
			for (String statement : statements) {
				// queries one by one.
				try {
					results.addAll(executeGenericQuery(statement, command));
				} catch (Throwable t) {
					if (ignoreErrors)
						t.printStackTrace();
					else
						throw t;
				}
			}
			return results;
		} else {
			// batch update.
			executeUpdate(statements, ignoreErrors);
			return new ArrayList<>();
		}
	}

	/**
	 * Batch execution of update commands. Everything that does not return a
	 * resultSet is an update.
	 * 
	 * @param statements
	 *            - Commands that are not related to BasicSelect
	 * @param ignoreErrors 
	 * @return
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	private void executeUpdate(List<String> statements, boolean ignoreErrors) throws DatabaseException, SQLException {
		Statement sqlStmt = null;
		try {
			if (!ignoreErrors) {
				// in case we don't have to catch exceptions we can run everything in a batch.
				sqlStmt = connection.createStatement();
				if (this.driverType == DriverType.MySql) {
					for (String s : statements) {
						try {
							sqlStmt.executeUpdate(s);
						} catch(Throwable t) {
							throw new SQLException("Executing update: " + s + " failed.",t);
						}
					}
					
				} else {
					for (String s : statements)
						sqlStmt.addBatch(s);
					sqlStmt.executeBatch();
				}
			} else {
				// IGNORE ERROR
				// run the statements one by one and handle exceptions.
				for (String s : statements) {
					sqlStmt = connection.createStatement();
					try {
						sqlStmt.executeUpdate(s);
					} catch(Throwable t) {
						if (debug) { 
							System.err.println("Error while executing " + s);
							t.printStackTrace();
						}
					}
				}
			}
		} catch (SQLException e) {
			SQLException looping = e;
			do {
				looping.printStackTrace();
				looping = looping.getNextException();
			} while (looping != null);
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
		return this.executeQuery(statements, command, null);
	}
	private List<Match> executeQuery(String statements, BasicSelect command, DataSink sink) throws DatabaseException, SQLException {
		List<Match> results = new ArrayList<>();
		ResultSet resultSet = null;
		Statement sqlStmt = null;
		try {
			connection.setAutoCommit(false);
			sqlStmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
	                   ResultSet.CONCUR_READ_ONLY);
			// execute the query
			sqlStmt.setFetchSize(1000);
			resultSet = sqlStmt.executeQuery(statements);
		} catch (Throwable t) {
			throw new DatabaseException("Error while executing query: " + statements, t);
		}
		try {
			// parse results
			Collection<Atom> readBuffer = new ArrayList<>();
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
				if (sink == null) {
					// create the match object for this record (fact)
					results.add(Match.create(command.getFormula(), map));
				} else {
					Atom queriedTable = command.getFormula().getAtoms()[0];
					List<Term> terms = new ArrayList<>();
					for (Term t : command.getFormula().getTerms()) {
						Term newTerm = map.get(t);
						if (newTerm != null)
							terms.add(newTerm);
					}
					readBuffer.add(Atom.create(queriedTable.getPredicate(), terms.toArray(new Term[terms.size()])));
					if (readBuffer.size()>1000) {
						sink.addFacts(readBuffer);
						readBuffer.clear();
					}
				}
			}
			if (readBuffer.size()>0) {
				sink.addFacts(readBuffer);
				readBuffer.clear();
			}
			
		}catch(IOException e) {
			throw new DatabaseException("Write error while reading data!",e);
		} finally {
			if (resultSet != null)
				resultSet.close();
			if (sqlStmt != null)
				sqlStmt.close();
			connection.setAutoCommit(true);
		}
		return results;
	}
	
	private List<String> executeGenericQuery(String statements, Command command) throws DatabaseException, SQLException {
		List<String> results = new ArrayList<>();
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
			for (int index = 1; index <= resultSet.getMetaData().getColumnCount(); index++) {
				results.add(resultSet.getMetaData().getColumnName(index));
			}
			for (int index = 1; index <= resultSet.getMetaData().getColumnCount(); index++) {
				results.add(resultSet.getMetaData().getColumnClassName(index));
			}
			// parse results
			while (resultSet.next()) {
				for (int index = 1; index <= resultSet.getMetaData().getColumnCount(); index++) {
					String line = resultSet.getString(index);
					results.add(line);
				}
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
		if (shutdown) {
			// already shutting down.
			return;
		}
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
	 * Uses the DriverManager to get a JDBC connection to an external database
	 */
	private static Connection getConnection(String driver, String url, String database, String username, String password) throws SQLException {
		if (!Strings.isNullOrEmpty(driver)) {
			try {
				Class.forName(driver).getConstructor().newInstance();
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new IllegalStateException("Could not load chase database driver '" + driver + "'",e);
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
			if (e.getMessage() != null && e.getMessage().contains("does not exist")) {
				System.err.println("Database " + database + " does not exists. falling back to default connection without database name.");
			} else if (e.getNextException()!=null)
				e.getNextException().printStackTrace();
			else
				e.printStackTrace();
		}
		Connection result = DriverManager.getConnection(url, username, password);
		result.setAutoCommit(true);
		return result;
	}	
}
