package uk.ac.ox.cs.pdq.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.data.sql.DatabaseException;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;

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
	 * @param parameters
	 *            - Parameters that specify the type of the database and describes
	 *            how to connect to it.
	 */
	protected abstract void initialiseConnections(DatabaseParameters parameters) throws DatabaseException;

	/**
	 * Closes the connection and optionally drops the database.
	 * 
	 * @param dropDatabase
	 */
	protected abstract void closeConnections(boolean dropDatabase) throws DatabaseException ;

	/**
	 * Creates a canonical database for the schema.
	 * 
	 * @param schema
	 * @throws DatabaseException
	 */
	protected abstract void initialiseDatabaseForSchema(Schema schema) throws DatabaseException;

	/**
	 * Drops the database.
	 */
	protected abstract void dropDatabase() throws DatabaseException ;

	/**
	 * Adds facts to the database. 
	 */
	protected abstract void addFacts(Collection<Atom> facts) throws DatabaseException;

	protected abstract void deleteFacts(Collection<Atom> facts) throws DatabaseException;

	/**
	 * Actual reading from the underlying data structure.
	 * 
	 * @return
	 */
	protected abstract Collection<Atom> getFactsFromPhysicalDatabase() throws DatabaseException;

	/**
	 * Gets all facts from relation R
	 * 
	 * @param r
	 * @return
	 */
	protected abstract Collection<Atom> getFactsOfRelation(Relation r) throws DatabaseException;

	protected abstract List<Match> answerQueries(Collection<PhysicalQuery> queries) throws DatabaseException;

	/**
	 * Executes a change in the database such as deleting facts or creating tables.
	 * 
	 * @param update
	 * @return
	 */
	protected abstract int executeUpdates(List<PhysicalDatabaseCommand> update) throws DatabaseException;
	
	
	protected static ArrayList<Atom> getAtomsFromMatches(List<Match> matches, Relation r) {
		ArrayList<Atom> ret = new ArrayList<>();
		for (Match m: matches) {
			List<Term> terms = new ArrayList<>();
			for (Term t:m.getFormula().getTerms()) {
				Term newTerm = m.getMapping().get(t);
				if (newTerm!=null)
					terms.add(newTerm);
			}
			ret.add(Atom.create(r, terms.toArray(new Term[terms.size()])));
		}
		return ret;
	}

	protected static ConjunctiveQuery createQuery(Relation r) {
		ArrayList<Variable> freeVariables = new ArrayList<>();
		ArrayList<Variable> body = new ArrayList<>();
		for (int i = 0; i < r.getAttributes().length; i++) {
			freeVariables.add(Variable.create("x"+i));
			body.add(Variable.create("x"+i));
		}
		return ConjunctiveQuery.create(freeVariables.toArray(new Variable[freeVariables.size()]), Atom.create(r,body.toArray(new Term[body.size()])));
	}
	
	public static ConjunctiveQuery createQuery(Relation r, String databaseInstanceID) {
		ArrayList<Variable> freeVariables = new ArrayList<>();
		ArrayList<Variable> body = new ArrayList<>();
		for (int i = 0; i < r.getAttributes().length-1; i++) {
			freeVariables.add(Variable.create("x"+i));
			body.add(Variable.create("x"+i));
		}
		Variable factID = Variable.create("DBFactID");
		body.add(factID);
		Conjunction conjunction = Conjunction.create(
				Atom.create(r,body.toArray(new Term[body.size()])), 
				Atom.create(VirtualMultiInstanceDatabaseManager.factIdInstanceIdMappingTable,new Term[] {factID,TypedConstant.create(databaseInstanceID)}));
		return ConjunctiveQuery.create(freeVariables.toArray(new Variable[freeVariables.size()]), conjunction );
	}

	public static Map<Variable, Constant> createProjectionMapping(Relation r, ConjunctiveQuery q) {
		Map<Variable, Constant> results = new HashMap<Variable, Constant>();
		for (int i = 0; i < r.getAttributes().length-1; i++) {
			results.put(Variable.create("x"+i), UntypedConstant.create("x"+i));
		}
		return results;
	}

	
}
