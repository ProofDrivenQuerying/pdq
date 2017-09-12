package uk.ac.ox.cs.pdq.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.db.sql.DerbyStatementBuilder;
import uk.ac.ox.cs.pdq.db.sql.MySQLStatementBuilder;
import uk.ac.ox.cs.pdq.db.sql.PostgresStatementBuilder;
import uk.ac.ox.cs.pdq.db.sql.SQLStatementBuilder;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;

/**
 * Models a database connection. It is responsible for creating database tables
 * for the input schema.
 * 
 * @author Gabor
 */
public class DatabaseConnection implements AutoCloseable {
	private int synchronousThreadsNumber = 1;

	private static Object LOCK = new Object();

	private boolean isInitialized = false;

	/** Open database connections. */
	// TOCOMMENT: this should be done with org.apache.commons.dbcp2 connection
	// pooling.
	protected List<Connection> synchronousConnections = Lists.newArrayList();

	/** Map schema relation to database tables. */
	private Map<String, Relation> relationNamesToDatabaseTables = null;

	/**
	 * Creates SQL statements to detect homomorphisms or add/delete facts in a
	 * database.
	 */
	private final SQLStatementBuilder builder;

	private final DatabaseParameters databaseParameters;

	private final Schema schema;

	protected boolean ready;

	private boolean isDerby = false;

	public DatabaseConnection(DatabaseParameters databaseParameters, Schema schema) throws SQLException {
		this(databaseParameters, schema, 1);
	}

	public DatabaseConnection(DatabaseParameters databaseParameters, Schema schema, int numberOfSynchConn) throws SQLException {
		this.synchronousThreadsNumber = numberOfSynchConn;
		String driver = databaseParameters.getDatabaseDriver();
		String url = databaseParameters.getConnectionUrl();
		String database = databaseParameters.getDatabaseName();
		String username = databaseParameters.getDatabaseUser();
		String password = databaseParameters.getDatabasePassword();
		if (url != null && url.contains("mysql")) {
			this.builder = new MySQLStatementBuilder();
		} else if (url != null && url.contains("postgres")) {
			this.builder = new PostgresStatementBuilder();
		} else {

			if (Strings.isNullOrEmpty(driver)) {
				driver = "org.apache.derby.jdbc.EmbeddedDriver";
			}
			if (Strings.isNullOrEmpty(url)) {
				url = "jdbc:derby:memory:tmp;create=true";
			}
			if (Strings.isNullOrEmpty(database)) {
				database = "chase";
			}
			// database name cannot be longer then 128 character, so if it is close we shorten it, 
			// add current time to make sure it is unique even in case of multiple runs.
			if (database.length() > 115) {
				database = database.substring(0, 100);
			}
			database += "_" + System.currentTimeMillis() + "_" + GlobalCounterProvider.getNext("DatabaseConnectionName");
			
			database = database.toUpperCase();
			databaseParameters.setDatabaseName(database);
			synchronized (LOCK) {
				username = "APP_" + GlobalCounterProvider.getNext("DatabaseConnectionName");
			}
			password = "";
			this.builder = new DerbyStatementBuilder();
		}

		for (int j = 0; j < synchronousThreadsNumber; j++)
			this.synchronousConnections.add(DatabaseUtilities.getConnection(driver, url, database, username, password));

		if (driver.contains("derby")) {
			isDerby = true;
			Statement st = this.synchronousConnections.get(0).createStatement();
			st.execute("create schema " + database);
		}
		this.schema = schema;
		this.databaseParameters = databaseParameters;
		this.relationNamesToDatabaseTables = new LinkedHashMap<>();

		if (!this.isInitialized) {
			this.setup();
			this.isInitialized = true;
		}
	}

	/**
	 * Sets up the database that will store the facts.
	 * 
	 * @throws SQLException
	 */
	protected void setup() throws SQLException {
		Statement sqlStatement = null;
		List<String> commandBuffer = new ArrayList<String>();
		try {
			sqlStatement = this.synchronousConnections.get(0).createStatement();
			for (String sql : this.builder.createDatabaseStatements(this.databaseParameters.getDatabaseName())) {
				commandBuffer.add(sql);
				sqlStatement.addBatch(sql);
			}
			// Create the database tables and create column indices
			for (Relation relation : this.schema.getRelations()) {
				Relation dbRelation = this.createDatabaseRelation(relation);
				this.relationNamesToDatabaseTables.put(relation.getName(), dbRelation);
				String command = this.builder.createTableStatement(dbRelation);
				sqlStatement.addBatch(command);
				commandBuffer.add(command);
			}
			sqlStatement.executeBatch();
		} catch (Throwable t) {
			if (sqlStatement != null) {
				System.err.println("SQL warnings: " + sqlStatement.getWarnings());
				System.err.println("Batch commands: " + commandBuffer);
			}
			t.printStackTrace();
			if (t instanceof java.sql.BatchUpdateException) {
				if (((java.sql.BatchUpdateException)t).getNextException() !=null)
					((java.sql.BatchUpdateException)t).getNextException().printStackTrace();
			}
			throw t;
		} finally {
			if (sqlStatement != null)
				sqlStatement.close();
		}
		ready = true;
	}

	/**
	 * Creates the db relation. Currently codes in the position numbers into the
	 * names, but this should change
	 *
	 * @param relation
	 *            the relation
	 * @return a new database relation with attributes x0,x1,...,x_{N-1}, Fact where
	 *         x_i maps to the i-th relation's attribute
	 */
	private Relation createDatabaseRelation(Relation relation) {
		String attrPrefix = "x";
		Attribute[] attributes = new Attribute[relation.getArity()];
		for (int index = 0; index < relation.getArity(); index++) {
			Attribute attribute = relation.getAttribute(index);
			if (Integer.class.isAssignableFrom((Class<?>) attribute.getType()) && attribute.getName().equals("InstanceID"))
				attributes[index] = Attribute.create(Integer.class, "InstanceID");
			else
				attributes[index] = Attribute.create(String.class, attrPrefix + index);
		}
		return Relation.create(relation.getName(), attributes, relation.isEquality());
	}

	/**
	 * Cleans up the database.
	 *
	 * @throws HomomorphismException
	 *             the homomorphism exception
	 * @throws SQLException
	 */
	protected void dropDatabase() throws SQLException {
		Statement sqlStatement = this.synchronousConnections.get(0).createStatement();
		try {
			for (String sql : this.builder.createDropStatements(this.databaseParameters.getDatabaseName())) {
				if (isDerby) {
					sqlStatement.execute(sql);
				} else {
					sqlStatement.addBatch(sql);
				}
			}
			if (!isDerby)
				sqlStatement.executeBatch();
		} catch (Exception e) {
			System.err.println("Error with command: " + this.builder.createDropStatements(this.databaseParameters.getDatabaseName()));
			throw e;
		} finally {
			sqlStatement.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() throws Exception {
		boolean drop = false;
		try {
			this.dropDatabase();
			drop = true;
		} catch (java.sql.BatchUpdateException bu) {
			Exception e = bu.getNextException();
			if (e != null) {
				e.printStackTrace();
			} else
				bu.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		if (!drop) {

		}
		try {
			for (Connection connection : this.synchronousConnections)
				connection.close();
			synchronousConnections.clear();

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public DatabaseConnection clone() {
		try {
			DatabaseConnection cloneCon = new DatabaseConnection(this.databaseParameters, this.schema);
			cloneCon.isInitialized = this.isInitialized;
			return cloneCon;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Map from relation names to the main memory objects existing for these names.
	 * TOCOMMENT: See issue 168
	 */
	public Map<String, Relation> getRelationNamesToDatabaseTables() {
		return this.relationNamesToDatabaseTables;
	}

	public SQLStatementBuilder getSQLStatementBuilder() {
		return this.builder;
	}

	public Schema getSchema() {
		return this.schema;
	}

	public List<Connection> getSynchronousConnections() {
		return this.synchronousConnections;
	}

	public Connection getSynchronousConnections(int index) {
		return this.synchronousConnections.get(index);
	}

	public DatabaseParameters getDatabaseParameters() {
		return this.databaseParameters;
	}

	public int getNumberOfSynchronousConnections() {
		return this.synchronousConnections.size();
	}

}
