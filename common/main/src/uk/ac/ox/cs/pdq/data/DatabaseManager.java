package uk.ac.ox.cs.pdq.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;

import uk.ac.ox.cs.pdq.data.cache.FactCache;
import uk.ac.ox.cs.pdq.data.memory.MemoryDatabaseInstance;
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
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;

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
	protected PhysicalDatabaseInstance databaseInstance;
	private String databaseName; // formal name, mainly for debugging purposes, default is "PdqTest"
	protected String databaseInstanceID; // unique ID generated for this instance.
	private Class<? extends PhysicalQuery> queryClass;
	/**
	 * Weather or not caching is allowed. In general it is always true expect when we use memory cache.
	 */
	protected boolean isMemoryDb=false;
	private FactCache cache=null;
	/**
	 * A database manager is active from the time it has successfully initialised
	 * connection(s) to a database until the connection(s) are closed.
	 */
	private boolean isActive = false;

	protected DatabaseManager(DatabaseParameters parameters) throws DatabaseException {
		this.parameters = (DatabaseParameters) parameters.clone();
		databaseName = parameters.getDatabaseName();
		if (databaseName == null) {
			databaseName = "PdqTest";
			this.parameters.setDatabaseName(databaseName);
		}
		databaseInstanceID = databaseName + "_" + System.currentTimeMillis() + "_" + this.hashCode();
		databaseInstance = initializeDatabaseInstance();
		initialiseConnections();
		if (!isMemoryDb && !(this instanceof VirtualMultiInstanceDatabaseManager)) {
			cache = new FactCache(databaseInstanceID);
		}
	}

	/**
	 * Creates database manager and connection if needed based on the parameters.
	 * 
	 * @param parameters
	 * @throws DatabaseException
	 */
	public static DatabaseManager create(DatabaseParameters parameters) throws DatabaseException {
		String virtual = parameters.getProperty("database.isvirtual");
		if (virtual != null && !virtual.isEmpty()) {
			if (Boolean.parseBoolean(virtual)) {
				return new VirtualMultiInstanceDatabaseManager(parameters);
			}
		}
		return new DatabaseManager(parameters);
	}

	/**
	 * Initialises an actual database instance such as SQLDatabaseInstance or
	 * in-memory DatabaseInstance.
	 * 
	 * @return
	 */
	private PhysicalDatabaseInstance initializeDatabaseInstance() {
		if (parameters.getDatabaseDriver().equalsIgnoreCase(MemoryDatabaseInstance.class.getName())) {
			isMemoryDb=true;
			queryClass = MemoryQuery.class;
			return new MemoryDatabaseInstance(parameters);
		}
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
		throw new NotImplementedException("Unknown database type: " + parameters.getDatabaseDriver());
	}

	public String getDatabaseInstanceID() {
		return databaseInstanceID;
	}

	public void setDatabaseInstanceID(String instanceID) {
		databaseInstanceID = instanceID;
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
		databaseInstance.initialiseConnections(parameters);
		isActive = true;
	}

	/**
	 * Closes the connection for this instance and all other instances that shares
	 * the connections with this one, and optionally drops the database.
	 * 
	 * @param dropDatabase
	 */
	public void shutdown(boolean dropDatabase) throws DatabaseException {
		databaseInstance.closeConnections(dropDatabase);
		isActive = false;
	}

	/**
	 * Creates a canonical database for the schema.
	 * 
	 * @param schema
	 * @throws DatabaseException
	 */
	public void initialiseDatabaseForSchema(Schema schema) throws DatabaseException {
		databaseInstance.initialiseDatabaseForSchema(schema);
	}

	/**
	 * Drops the database.
	 */
	public void dropDatabase() throws DatabaseException {
		databaseInstance.dropDatabase();
	}

	public Collection<Atom> addFacts(Collection<Atom> facts) throws DatabaseException {
		if (isMemoryDb || this instanceof VirtualMultiInstanceDatabaseManager) {
			databaseInstance.addFacts(facts);
			return new ArrayList<>();
		}
		Collection<Atom> newFacts = cache.addFacts(facts);
		// only add whats new
		databaseInstance.addFacts(newFacts);
		return newFacts;
	}

	public void deleteFacts(Collection<Atom> facts) throws DatabaseException {
		databaseInstance.deleteFacts(facts);
		if (!isMemoryDb && !(this instanceof VirtualMultiInstanceDatabaseManager)) {
			cache.removeFacts(facts);
		}
	}

	/**
	 * In case the implementation has in-memory cache this can be used to get the
	 * cached data.
	 * 
	 * @return
	 */
	public Collection<Atom> getCachedFacts() throws DatabaseException {
		if (isMemoryDb) {
			// in case of memory db the physical and the cached is the same.
			return databaseInstance.getFactsFromPhysicalDatabase();
		}
		
		if (this instanceof VirtualMultiInstanceDatabaseManager) {
			throw new DatabaseException("Caching is disabled.");
		}
		return cache.getFacts();
	}

	/**
	 * Actual reading from the underlying data structure.
	 * 
	 * @return
	 */
	public Collection<Atom> getFactsFromPhysicalDatabase() throws DatabaseException {
		return databaseInstance.getFactsFromPhysicalDatabase();
	}

	public List<Match> answerQueries(Collection<PhysicalQuery> queries) throws DatabaseException {
		return databaseInstance.answerQueries(queries);
	}
	
	public List<Match> answerQueryDifferences(ConjunctiveQuery leftQuery, ConjunctiveQuery rightQuery) throws DatabaseException {
		Collection<PhysicalQuery> queries = new ArrayList<>();
		queries.add(PhysicalQuery.createQueryDifference(this, leftQuery, rightQuery));
		return databaseInstance.answerQueries(queries);
	}

	/**
	 * Executes a change in the database such as deleting facts or creating tables.
	 * 
	 * @param update
	 * @return
	 */
	public int executeUpdates(List<PhysicalDatabaseCommand> update) throws DatabaseException {
		return databaseInstance.executeUpdates(update);
	}

	/**
	 * Has active (not closed but initialised) connections.
	 * 
	 * @return
	 */
	public boolean isActive() {
		return isActive;
	}

	protected Class<? extends PhysicalQuery> getQueryClass() {
		return queryClass;
	}

}
