package uk.ac.ox.cs.pdq.data.memory;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.data.PhysicalDatabaseCommand;
import uk.ac.ox.cs.pdq.data.PhysicalDatabaseInstance;
import uk.ac.ox.cs.pdq.data.PhysicalQuery;
import uk.ac.ox.cs.pdq.data.sql.DatabaseException;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;

/**
 * Represents a no-sql database instance that keeps facts in the memory and
 * allows querying them.
 * 
 * @author Gabor
 *
 */
public class MemoryDatabaseInstance extends PhysicalDatabaseInstance {

	public MemoryDatabaseInstance(DatabaseParameters parameters) {
	}

	@Override
	protected void initialiseConnections(DatabaseParameters parameters) {

	}

	@Override
	protected void closeConnections(boolean dropDatabase) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initialiseDatabaseForSchema(Schema schema) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void dropDatabase() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void addFacts(Collection<Atom> facts) throws DatabaseException {
	}

	@Override
	protected void deleteFacts(Collection<Atom> facts) {
		// TODO Auto-generated method stub

	}

	@Override
	protected Collection<Atom> getFactsFromPhysicalDatabase() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<Match> answerQueries(Collection<PhysicalQuery> queries) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int executeUpdates(List<PhysicalDatabaseCommand> update) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected Collection<Atom> getFactsOfRelation(Relation r) {
		// TODO Auto-generated method stub
		return null;
	}
}
