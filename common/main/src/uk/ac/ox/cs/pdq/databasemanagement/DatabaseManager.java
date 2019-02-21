package uk.ac.ox.cs.pdq.databasemanagement;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;

/**
 * This interface describes the public functions of any Database Manager. The
 * main functionality of a database manager is to store and manage facts,
 * optionally maintain a cache and answer queries.
 * 
 * @author Gabor
 *
 */
public interface DatabaseManager {
	/**
	 * Creates an empty canonical database for the schema. Table names will be the
	 * same as the relation names, and the attribute names will be the same as the
	 * attribute names. Thanks to this those names have to conform the database
	 * language rules, usually max 126 character long names, starting with letters
	 * and containing only letters and numbers.
	 * 
	 * After creating the DatabaseManager either this function have to be called to
	 * create tables, or the setSchema function have to be called to let make sure
	 * the DatabaseManager knows what kind of tables are there in the database.
	 * 
	 * @param schema
	 * @throws DatabaseException
	 */
	public void initialiseDatabaseForSchema(Schema schema) throws DatabaseException;

	/**
	 * Stores this fact as a record in the database. Table name will be the same as
	 * the predicate name, column names will be the same as the attribute names in
	 * the relation.
	 * 
	 * For this call to work you have to have the database initialised for this
	 * schema. The software cannot check for this initialisation, since it have to
	 * be done only once (to create the tables) and then the database manager by
	 * connecting to the same database can continue work on the pre-initialised
	 * database.
	 * 
	 * @param facts
	 * @throws DatabaseException
	 */
	public void addFacts(Collection<Atom> facts) throws DatabaseException;

	/**
	 * Deletes a list of facts from the database one by one. (bulk delete is not
	 * implemented yet)
	 * 
	 * @param facts
	 * @throws DatabaseException
	 */
	public void deleteFacts(Collection<Atom> facts) throws DatabaseException;

	/**
	 * Actual reading from the underlying data structure.
	 * 
	 * @return
	 */
	public Collection<Atom> getFactsFromPhysicalDatabase() throws DatabaseException;
	/**
	 * In case the implementation has in-memory cache this can be used to get the
	 * cached data, otherwise it should return the same as the previous function.
	 * 
	 * @return
	 */
	public Collection<Atom> getCachedFacts() throws DatabaseException;

	/**
	 * A list of CQs to be executed parallel. All results are gathered and added to
	 * the list.
	 * 
	 * @param queries
	 * @return
	 * @throws DatabaseException
	 */
	public List<Match> answerConjunctiveQueries(Collection<ConjunctiveQuery> queries) throws DatabaseException;
	
	/** Same as above but only one query
	 * @param query
	 * @return
	 * @throws DatabaseException
	 */
	public List<Match> answerConjunctiveQuery(ConjunctiveQuery query) throws DatabaseException;

	/**
	 * Represent a kind of nested select that will tell the difference between two
	 * BasicSelects.
	 * 
	 * <pre>
	 * Example: 
	 * Left query: exists[x,y](R(x,y,z) & S(x,y))
	 * Right query:exists[x,y,z](R(x,y,z) & (S(x,y) & T(z,res1,res2)))
	 * 
	 * The result will be all facts that only satisfy the left query, but not the right one.
	 * 
	 * </pre>
	 * 
	 * @param leftQuery
	 * @param rightQuery
	 * @return
	 * @throws DatabaseException
	 */
	public List<Match> answerQueryDifferences(ConjunctiveQuery leftQuery, ConjunctiveQuery rightQuery) throws DatabaseException;

	/**
	 * Drops the database. For safety reasons it recreates the same database, and
	 * leaves the empty database there. This is needed since most database provider
	 * will not allow remote connection to a none existing database, so we would
	 * force the user to manually create an empty database after each usage of this
	 * system.
	 */
	public void dropDatabase() throws DatabaseException;

	/**
	 * Closes the connection for this instance and all other instances that shares
	 * the connections with this one
	 */
	public void shutdown() throws DatabaseException;

	/**
	 * This function is needed when you connect to existing database that you do not
	 * want to initialise for this schema because it already contains the required
	 * tables (and maybe some data as well)
	 * 
	 * @param s
	 */
	public void setSchema(Schema s);

	/**
	 * @return the last schema we initialised to
	 */
	public Schema getSchema();

	/**
	 * @return the current DatabaseID that was set by the setDatabaseInstanceID or
	 *         the one that was generated/set at initialisation time.
	 */
	public int getDatabaseInstanceID();

	/**
	 * @return optional name of this database
	 */
	public String getDatabaseName();

	public DatabaseManager clone(int instanceId) throws DatabaseException;
	/**
	 * Adds an extra relation to the existing schema, and creates the new table in the database.
	 * 
	 * @param newRelation
	 * @throws DatabaseException
	 */
	public void addRelation(Relation newRelation) throws DatabaseException;

	/** In case the database manager can explain a query, this function will return the cost of this query.
	 * @param cq
	 * @return
	 */
	public List<String> executeQueryExplain(ConjunctiveQuery cq) throws DatabaseException;

	/**
	 * Maps each constant to a list of atoms that have the same constant.
	 * We need this table when we are applying an EGD chase step, to easily find all facts that has an obsolete
	 * constant in order to update them with the new representative constant. 
	 * Could be replaced with a query.
	 **/
	public void addToConstantsToAtoms(Constant term, Atom atom) throws DatabaseException ;

	public Collection<Atom> getAtomsContainingConstant(Constant obsoleteConstant) throws DatabaseException ;

	public void removeConstantFromMap(Constant obsoleteConstant);
	
	public void mergeConstantsToAtomsMap(DatabaseManager from);
}
