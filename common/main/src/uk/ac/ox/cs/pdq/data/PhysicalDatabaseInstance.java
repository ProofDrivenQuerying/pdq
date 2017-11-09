package uk.ac.ox.cs.pdq.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.data.sql.DatabaseException;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
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

	protected static ArrayList<Atom> getAtomsFromMatches(List<Match> matches, Relation r) {
		ArrayList<Atom> ret = new ArrayList<>();
		Predicate predicate = Predicate.create(r.getName(), r.getArity(),r.isEquality());
		for (Match m: matches) {
			List<Term> terms = new ArrayList<>();
			for (Term t:m.getFormula().getTerms()) {
				Term newTerm = m.getMapping().get(t);
				if (newTerm!=null)
					terms.add(newTerm);
			}
			ret.add(Atom.create(predicate, terms.toArray(new Term[terms.size()])));
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
}
