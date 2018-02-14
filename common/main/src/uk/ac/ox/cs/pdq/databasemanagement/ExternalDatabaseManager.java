package uk.ac.ox.cs.pdq.databasemanagement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.databasemanagement.execution.ExecutionManager;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.BasicSelect;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.BulkInsert;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.Command;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.CreateDatabase;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.CreateTable;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.Delete;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.DifferenceQuery;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.DropDatabase;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Term;

/**
 * Simplest external database manager. Creates and manages connections,
 * different SQL database dialects.
 * 
 * No sub classes of this package should be accessed directly, everything goes
 * through this manager class. <br>
 * 
 * This database manager does not handle duplicated facts, it will throw an
 * exception when the addFacts is called with a record that already exists in
 * the database. <br>
 * 
 *  Main features: <br>
 * <li>- add/delete facts
 * <li>- answer queries
 * <li>- it can be used without knowing what is the underlying database
 * implementation</li><br>
 * <li>- it can connect to an existing database, or</li><br>
 * <li>- it can create a new empty database, create tables in it and then drop
 * it when it is not needed anymore.</li><br>
 *
 * This manager does not have any cache, all data is in the database.
 * 
 * @author Gabor
 *
 */
public class ExternalDatabaseManager implements DatabaseManager {
	/**
	 * Database parameters such as connection url, credentials etc.
	 */
	protected DatabaseParameters parameters;
	protected String databaseName; // formal name, mainly for debugging purposes, default is "PdqTest"
	/**
	 * Flag to show that we are after initialisation but before dropping the database. 
	 */
	protected boolean databaseExists = false;
	/**
	 * The execution manager is responsible to manage connections and parallel
	 * execution of bulk requests.
	 */
	private ExecutionManager executor;
	/**
	 * A database manager is active from the time it has successfully initialised
	 * connection(s) to a database until the connection(s) are closed.
	 */
	protected Schema schema;

	/**
	 * Creates a database manager. Initialises connections by creating an executor
	 * manager.
	 * 
	 * <pre>
	 * Usage:
	 *  - step 1: 
	 * 		Construct with the Database parameters, and then 
	 *  - step 2:
	 *  	Call the initialiseDatabaseForSchema or the setSchema function. The first will create
	 * 		the relations as tables, the other assumes they are already created.
	 *  - step 3:
	 *    	use the database (get facts, add facts, execute queries, etc)
	 *  - step 4 (optional) :
	 *   	drop the database
	 *  - step 5: 
	 *    	shut down the database manager by calling the shutdown function. will not delete any data 
	 *    	(unless the database provider is a memory database)
	 * </pre>
	 * 
	 * @param parameters
	 *            database parameters, URLs, login names etc.
	 * @throws DatabaseException
	 *             - in case connection to the database provider fails.
	 */
	public ExternalDatabaseManager(DatabaseParameters parameters) throws DatabaseException {
		this.parameters = (DatabaseParameters) parameters.clone();
		databaseName = parameters.getDatabaseName();

		// default database name
		if (databaseName == null) {
			databaseName = "PdqTest";
			this.parameters.setDatabaseName(databaseName);
		}

		// Execution manager.
		executor = new ExecutionManager(this.parameters);
	}

	/**
	 * Empty constructor for the Memory Database Manager.
	 * 
	 * @throws DatabaseException
	 */
	protected ExternalDatabaseManager() throws DatabaseException {
	}

	// INTERFACE FUNCTIONS
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
	public void initialiseDatabaseForSchema(Schema schema) throws DatabaseException {
		this.schema = schema;
		executor.execute(new CreateDatabase(schema));
		executor.execute(new CreateTable(schema.getRelations(), parameters.isFactsAreUnique()));
		databaseExists = true;
	}

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
	public void addFacts(Collection<Atom> facts) throws DatabaseException {
		executor.execute(new BulkInsert(facts, schema));
	}

	/**
	 * Deletes a list of facts from the database one by one. (bulk delete is not
	 * implemented yet)
	 * 
	 * @param facts
	 * @throws DatabaseException
	 */
	public void deleteFacts(Collection<Atom> facts) throws DatabaseException {
		List<Command> deletes = new ArrayList<>();
		for (Atom a : facts) {
			deletes.add(new Delete(a, schema));
		}
		executor.execute(deletes);
	}

	/**
	 * Does not have a cache so we will read the actual data from the external
	 * source.
	 * 
	 * @return
	 */
	public Collection<Atom> getCachedFacts() throws DatabaseException {
		return getFactsFromPhysicalDatabase();
	}

	/**
	 * Actual reading from the underlying data structure.
	 * 
	 * @return
	 */
	public Collection<Atom> getFactsFromPhysicalDatabase() throws DatabaseException {
		List<Command> queries = new ArrayList<>();
		for (Relation r : schema.getRelations()) {
			queries.add(new BasicSelect(r));
		}
		return convertMatchesToAtoms(executor.execute(queries), queries);
	}

	/**
	 * A list of CQs to be executed parallel. All results are gathered and added to
	 * the list.
	 * 
	 * @param queries
	 * @return
	 * @throws DatabaseException
	 */
	public List<Match> answerConjunctiveQueries(Collection<ConjunctiveQuery> queries) throws DatabaseException {
		List<Command> commands = new ArrayList<>();
		// convert CQs to BasicSelect sql commands.
		for (ConjunctiveQuery cq : queries) {
			BasicSelect q = new BasicSelect(this.schema, cq);
			commands.add(q);
		}
		// execute the SQL command
		return executor.execute(commands);
	}
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager#answerConjunctiveQuery(uk.ac.ox.cs.pdq.fol.ConjunctiveQuery)
	 */
	public List<Match> answerConjunctiveQuery(ConjunctiveQuery query) throws DatabaseException {
		return answerConjunctiveQueries(Arrays.asList(new ConjunctiveQuery[] {query}));
	}

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
	public List<Match> answerQueryDifferences(ConjunctiveQuery leftQuery, ConjunctiveQuery rightQuery) throws DatabaseException {
		DifferenceQuery diff = new DifferenceQuery(leftQuery, rightQuery, schema);
		return executor.execute(Arrays.asList(new Command[] { diff }));
	}

	/**
	 * Drops the database. For safety reasons it recreates the same database, and
	 * leaves the empty database there. This is needed since most database provider
	 * will not allow remote connection to a none existing database, so we would
	 * force the user to manually create an empty database after each usage of this
	 * system.
	 */
	public void dropDatabase() throws DatabaseException {
		if (databaseExists) executor.execute(new DropDatabase(schema));
		databaseExists = false;
	}

	/**
	 * Closes the connection for this instance and all other instances that shares
	 * the connections with this one
	 */
	public void shutdown() throws DatabaseException {
		if (databaseExists) {
			new Exception("Warning, database manager is shutting down, but the database is not dropped yet.").printStackTrace();
		}
		executor.shutdown();
	}

	/**
	 * This function is needed when you connect to existing database that you do not
	 * want to initialise for this schema because it already contains the required
	 * tables (and maybe some data as well)
	 * 
	 * @param s
	 */
	public void setSchema(Schema s) {
		this.schema = s;
	}

	/**
	 * @return the last schema we initialized to
	 */
	public Schema getSchema() {
		return schema;
	}

	public int getDatabaseInstanceID() {
		return this.hashCode();
	}

	public String getDatabaseName() {
		return databaseName;
	}

	/**
	 * The get facts from physical database function returns a list of Atoms instead
	 * of Matches. This function helps to convert from one to the other.
	 * 
	 * @param matches
	 * @param queries
	 * @return
	 * @throws DatabaseException
	 */
	protected static List<Atom> convertMatchesToAtoms(List<Match> matches, List<Command> queries) throws DatabaseException {
		List<Atom> ret = new ArrayList<>();
		for (Match m : matches) {
			if (m.getFormula().getAtoms().length > 1) {
				// The results of query "select * from tableX" can be easily parsed by knowing
				// the attributes of tableX, however a composite result would require to create
				// a new predicate representing the result columns, and that is not allowed
				// here.
				throw new DatabaseException("Only single table query results can be converted to a list of atoms!");
			}
			Atom queriedTable = m.getFormula().getAtoms()[0];
			List<Term> terms = new ArrayList<>();
			for (Term t : m.getFormula().getTerms()) {
				Term newTerm = m.getMapping().get(t);
				if (newTerm != null)
					terms.add(newTerm);
			}
			ret.add(Atom.create(queriedTable.getPredicate(), terms.toArray(new Term[terms.size()])));
		}
		return ret;
	}

	protected void executeUpdateCommand(Command command) throws DatabaseException {
		executor.execute(command);
	}

	@Override
	public DatabaseManager clone(int instanceId) throws DatabaseException {
		throw new DatabaseException("Database manager cannot be cloned.");
	}

	/**
	 * Adds an extra relation to the existing schema, updates the extended schema
	 * accordingly, and creates the new table in the database.
	 * 
	 * @param newRelation
	 * @throws DatabaseException
	 */
	public void addRelation(Relation newRelation) throws DatabaseException {
		Relation newRelations[] = new Relation[this.schema.getRelations().length + 1];
		int i = 0;
		for (Relation r : this.schema.getRelations())
			newRelations[i++] = r;
		newRelations[i] = newRelation;
		List<Dependency> deps = new ArrayList<>();
		deps.addAll(Arrays.asList(this.schema.getKeyDependencies()));
		deps.addAll(Arrays.asList(this.schema.getNonEgdDependencies()));
		this.schema = new Schema(newRelations, deps.toArray(new Dependency[deps.size()]));
		executeUpdateCommand(new CreateTable(this.schema.getRelation(newRelation.getName()), parameters.isFactsAreUnique()));
	}

	
}
