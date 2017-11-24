package uk.ac.ox.cs.pdq.databasemanagement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.databasemanagement.execution.ExecutionManager;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.BulkInsert;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.Command;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.CreateDatabase;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.CreateTable;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.Delete;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.DropDatabase;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.Query;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.QueryDifference;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Term;

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
	private String databaseName; // formal name, mainly for debugging purposes, default is "PdqTest"
	protected int databaseInstanceID; // unique ID generated for this instance.
	private ExecutionManager executor;
	/**
	 * A database manager is active from the time it has successfully initialised
	 * connection(s) to a database until the connection(s) are closed.
	 */
	private Schema schema;

	public DatabaseManager(DatabaseParameters parameters) throws DatabaseException {
		this.parameters = (DatabaseParameters) parameters.clone();
		databaseName = parameters.getDatabaseName();
		if (databaseName == null) {
			databaseName = "PdqTest";
			this.parameters.setDatabaseName(databaseName);
		}
		databaseInstanceID = this.hashCode();
		executor = new ExecutionManager(this.parameters);
	}

	public int getDatabaseInstanceID() {
		return databaseInstanceID;
	}

	public void setDatabaseInstanceID(int instanceID) {
		databaseInstanceID = instanceID;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	/**
	 * Closes the connection for this instance and all other instances that shares
	 * the connections with this one
	 */
	public void shutdown() throws DatabaseException {
		executor.shutdown();
	}

	/**
	 * Creates a canonical database for the schema.
	 * 
	 * @param schema
	 * @throws DatabaseException
	 */
	public void initialiseDatabaseForSchema(Schema schema) throws DatabaseException {
		this.schema = schema;	
		executor.execute(new CreateDatabase());
		executor.execute(new CreateTable(schema.getRelations()));
	}

	/**
	 * Drops the database.
	 */
	public void dropDatabase() throws DatabaseException {
		executor.execute(new DropDatabase());
	}

	public void addFacts(Collection<Atom> facts) throws DatabaseException {
//		List<Command> inserts = new ArrayList<>();
//		for (Atom a: facts) {
//			inserts.add(new Insert(a,schema));
//		}
//		executor.execute(inserts);
		executor.execute(new BulkInsert(facts, schema));
	}

	public void deleteFacts(Collection<Atom> facts) throws DatabaseException {
		List<Command> deletes = new ArrayList<>();
		for (Atom a: facts) {
			deletes.add(new Delete(a,schema));
		}
		executor.execute(deletes);
	}

	/**
	 * Actual reading from the underlying data structure.
	 * 
	 * @return
	 */
	public Collection<Atom> getFactsFromPhysicalDatabase() throws DatabaseException {
		List<Command> queries = new ArrayList<>();
		for (Relation r: schema.getRelations()) {
			queries.add(new Query(r));
		}
		return convertMatchesToAtoms(executor.execute(queries),queries); 
	}

	public List<Match> answerQueries(Collection<ConjunctiveQuery> queries) throws DatabaseException {
		List<Command> commands = new ArrayList<>();
		for (ConjunctiveQuery cq:queries) {
			Query q = new Query(this.schema, cq);
			commands.add(q);
		}
		return executor.execute(commands);
	}
	
	protected static List<Atom> convertMatchesToAtoms(List<Match> matches, List<Command> queries) throws DatabaseException {
		List<Atom> ret = new ArrayList<>();
		for (Match m: matches) {
			if (m.getFormula().getAtoms().length > 1) {
				throw new DatabaseException("Only single table query results can be converted to a list of atoms!");
			}
			Atom queriedTable = m.getFormula().getAtoms()[0];
			List<Term> terms = new ArrayList<>();
			for (Term t:m.getFormula().getTerms()) {
				Term newTerm = m.getMapping().get(t);
				if (newTerm!=null)
					terms.add(newTerm);
			}
			ret.add(Atom.create(queriedTable.getPredicate(), terms.toArray(new Term[terms.size()])));
		}
		return ret;
	}

	public List<Match> answerQueryDifferences(ConjunctiveQuery leftQuery, ConjunctiveQuery rightQuery) throws DatabaseException {
		QueryDifference diff = new QueryDifference(leftQuery, rightQuery, schema);
		return executor.execute(Arrays.asList(new Command[] {diff}));
	}

	protected Schema getSchema() {
		return schema;
	}

}
