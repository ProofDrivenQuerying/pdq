package uk.ac.ox.cs.pdq.data;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.data.sql.DatabaseException;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;

/**
 * Represents a physical database instance such as a postgres sql database. All
 * functions are "default" visibility since only the DatabaseManager can use
 * them.
 * 
 * @author Gabor
 *
 */
public abstract class PhysicalDatabaseInstance {

	/**
	 * Creates external connections if needed. For example a Postges SQL
	 * implementation will attempt to connect to the remote server. In-memory
	 * database will only initialise itself using the given parameters.
	 * 
	 * @param parameters - Parameters that specify the type of the database and describes how to connect to it.
	 * @param existingInstances - to avoid using too many connections the existing instances can be listed and the connections can be shared.
	 */
	protected abstract void initialiseConnections(DatabaseParameters parameters, List<PhysicalDatabaseInstance> existingInstances) throws DatabaseException;

	/**
	 * Closes the connection and optionally drops the database.
	 * 
	 * @param dropDatabase
	 */
	protected abstract void closeConnections(boolean dropDatabase);

	/**
	 * Creates a canonical database for the schema.
	 * 
	 * @param schema
	 */
	protected abstract void initialiseDatabaseForSchema(Schema schema);

	/**
	 * Drops the database.
	 */
	protected abstract void dropDatabase();

	/**
	 * Adds facts to the datbase. Returns the added facts. If this database allows
	 * duplicates it will return the input facts, but if it is not then it will
	 * return a subset that contains only the new facts.
	 * 
	 * @param facts
	 * @return
	 */
	protected abstract Collection<Atom> addFacts(Collection<Atom> facts);

	protected abstract void deleteFacts(Collection<Atom> facts);

	/**
	 * Opposite of addfacts, the actual implementation decides if it will be given
	 * from cache or by reading the database.
	 * 
	 * @return
	 */
	protected abstract Collection<Atom> getFacts();

	/**
	 * In case the implementation has in-memory cache this can be used to get the
	 * cached data.
	 * 
	 * @return
	 */
	protected abstract Collection<Atom> getCachedFacts();

	/**
	 * Actual reading from the underlying data structure.
	 * 
	 * @return
	 */
	protected abstract Collection<Atom> getFactsFromPhysicalDatabase();

	protected abstract List<Match> answerQueries(List<PhysicalQuery> queries);

	/**
	 * Executes a change in the database such as deleting facts or creating tables.
	 * 
	 * @param update
	 * @return
	 */
	protected abstract int executeUpdates(List<PhysicalDatabaseCommand> update);
}
