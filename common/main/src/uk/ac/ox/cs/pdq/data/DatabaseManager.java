package uk.ac.ox.cs.pdq.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;

import uk.ac.ox.cs.pdq.data.memory.MemoryQuery;
import uk.ac.ox.cs.pdq.data.sql.DatabaseException;
import uk.ac.ox.cs.pdq.data.sql.DerbyDatabaseInstance;
import uk.ac.ox.cs.pdq.data.sql.MySqlDatabaseInstance;
import uk.ac.ox.cs.pdq.data.sql.PostgresDatabaseInstance;
import uk.ac.ox.cs.pdq.data.sql.SQLQuery;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;

/**
 * Main database management entry point. Creates and manages connections,
 * different database representations ( SQL or in-memory). No sub classes should
 * be accessed directly, everything goes through this class. <br>
 * Main features: <br>
 * <li>- it can be used without knowing what is the underlying database
 * implementation</li><br>
 * <li>- it can connect to an existing database, or</li><br>
 * <li>- it can create a new empty database, create tables in it and then drop
 * it when it is not needed anymore.</li><br>
 * 
 * @author Gabor
 *
 */
public class DatabaseManager {
	private DatabaseParameters parameters;
	private PhysicalDatabaseInstance databaseInstance;
	private String databaseName; // formal name, mainly for debugging purposes, default is "PdqTest"
	private String databaseInstanceID; // unique ID generated for this instance.
	private boolean isVirtualDatabase = true;
	private Class<? extends PhysicalQuery> queryClass;
	/**
	 * A database manager is active from the time it has successfully initialised connection(s) to a database until the connection(s) are closed.
	 */
	private boolean isActive = false;
	private static List<DatabaseManager> managers = new ArrayList<>(); 

	/**
	 * Creates database manager and connection if needed based on the parameters.
	 * 
	 * @param parameters
	 * @throws DatabaseException 
	 */
	public DatabaseManager(DatabaseParameters parameters) throws DatabaseException {
		this.parameters = (DatabaseParameters) parameters.clone();
		databaseName = parameters.getDatabaseName();
		if (databaseName == null) {
			databaseName = "PdqTest";
			this.parameters.setDatabaseName(databaseName);
		}
		String virtual = parameters.getProperty("database.isvirtual");
		if (virtual!=null && !virtual.isEmpty()) {
			isVirtualDatabase = Boolean.parseBoolean(virtual);
		}
		databaseInstanceID = databaseName + "_" + System.currentTimeMillis() + "_" + this.hashCode();
		databaseInstance = initializeDatabaseInstance();
		initialiseConnections();
		managers.add(this);
	}

	/**
	 * Creates a new DatabaseManager from an existing one. Shares the database
	 * connections but points to a different database, different set of facts.
	 * 
	 * @param parent
	 * @param newDatabaseName
	 * @throws DatabaseException 
	 */
	public DatabaseManager(DatabaseManager parent, String newDatabaseName) throws DatabaseException {
		databaseName = newDatabaseName;
		this.parameters = (DatabaseParameters) parent.parameters.clone();
		this.parameters.setDatabaseName(databaseName);
		databaseInstanceID = databaseName + "_" + System.currentTimeMillis() + "_" + this.hashCode();
		isVirtualDatabase = parent.isVirtualDatabase();
		databaseInstance = initializeDatabaseInstance();
		initialiseConnections();
		managers.add(this);
	}

	/**
	 * Initialises an actual database instance such as SQLDatabaseInstance or
	 * in-memory DatabaseInstance.
	 * 
	 * @return
	 */
	private PhysicalDatabaseInstance initializeDatabaseInstance() {
		if (parameters.getDatabaseDriver().contains("postgres")) {
			queryClass = SQLQuery.class;
			return new PostgresDatabaseInstance(parameters);
		}
		if (parameters.getDatabaseDriver().contains("derby")) {
			queryClass = SQLQuery.class;
			return new DerbyDatabaseInstance(parameters);
		}
		if (parameters.getDatabaseDriver().contains("mysql")) {
			queryClass = SQLQuery.class;
			return new MySqlDatabaseInstance(parameters);
		}
		if (parameters.getDatabaseDriver().contains("memory")) {
			queryClass = MemoryQuery.class;
			return new MySqlDatabaseInstance(parameters);
		}
		throw new NotImplementedException("Unknown database type: " + parameters.getDatabaseDriver());
	}

	public String getDatabaseInstanceID() {
		return databaseInstanceID;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	/**
	 * Creates external connections if needed. For example a Postges SQL
	 * implementation will attempt to connect to the remote server. In-memory
	 * database will only initialise itself using the given parameters.
	 * 
	 * Connections are shared across DatabaseManagers
	 * 
	 * @param parameters
	 * @throws DatabaseException 
	 */
	private void initialiseConnections() throws DatabaseException {
		List<PhysicalDatabaseInstance> instances = new ArrayList<>(); 
		for (DatabaseManager manager:managers) {
			if (manager.isActive) {
				instances.add(manager.databaseInstance);
			}
		}
		databaseInstance.initialiseConnections(parameters,instances);
		isActive = true;
	}
	
	/**
	 * Closes the connection for this instance and all other instances that shares the connections with this one, and optionally drops the database.
	 * 
	 * @param dropDatabase
	 */
	public void closeConnections(boolean dropDatabase) {
		databaseInstance.closeConnections(dropDatabase);
		isActive = false;
		managers.remove(this);
	}

	/**
	 * Creates a canonical database for the schema.
	 * 
	 * @param schema
	 */
	public void initialiseDatabaseForSchema(Schema schema) {
		databaseInstance.initialiseDatabaseForSchema(schema);
	}

	/**
	 * Drops the database.
	 */
	public void dropDatabase() {
		databaseInstance.dropDatabase();
	}

	public Collection<Atom> addFacts(Collection<Atom> facts) {
		return databaseInstance.addFacts(facts);
	}

	public void deleteFacts(Collection<Atom> facts) {
		databaseInstance.deleteFacts(facts);
	}

	/**
	 * Opposite of addfacts, the actual implementation decides if it will be given
	 * from cache or by reading the database.
	 * 
	 * @return
	 */
	public Collection<Atom> getFacts() {
		return databaseInstance.getFacts();
	}

	/**
	 * In case the implementation has in-memory cache this can be used to get the
	 * cached data.
	 * 
	 * @return
	 */
	public Collection<Atom> getCachedFacts() {
		return databaseInstance.getCachedFacts();
	}

	/**
	 * Actual reading from the underlying data structure.
	 * 
	 * @return
	 */
	public Collection<Atom> getFactsFromPhysicalDatabase() {
		return databaseInstance.getFactsFromPhysicalDatabase();
	}

	public List<Match> answerQueries(List<PhysicalQuery> queries) {
		return databaseInstance.answerQueries(queries);
	}

	/**
	 * Executes a change in the database such as deleting facts or creating tables.
	 * 
	 * @param update
	 * @return
	 */
	public int executeUpdates(List<PhysicalDatabaseCommand> update) {
		return databaseInstance.executeUpdates(update);
	}

	/** Has active (not closed but initialised) connections.
	 * @return
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * Closes all connections, closes all instances.
	 */
	public static void shutdown() {
		for (DatabaseManager manager:managers) {
			if (manager.isActive) {
				manager.closeConnections(false);
			}
		}
	}

	public boolean isVirtualDatabase() {
		return isVirtualDatabase;
	}
	
	protected Class<? extends PhysicalQuery> getQueryClass() {
		return queryClass;
	}
}
