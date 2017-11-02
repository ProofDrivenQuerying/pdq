package uk.ac.ox.cs.pdq.data.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.data.PhysicalDatabaseCommand;
import uk.ac.ox.cs.pdq.data.PhysicalDatabaseInstance;
import uk.ac.ox.cs.pdq.data.PhysicalQuery;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;

/**
 * Represents a physical database that speaks SQL. Such as Derby, MySQL or
 * PostgresSQL.
 * 
 * @author Gabor
 *
 */
public abstract class SqlDatabaseInstance extends PhysicalDatabaseInstance {
	protected SQLDatabaseConnection connection;
	
	public SqlDatabaseInstance(DatabaseParameters parameters) {
	}

	@Override
	protected synchronized void initialiseConnections(DatabaseParameters parameters,List<PhysicalDatabaseInstance> existingInstances) throws DatabaseException {
		try {
			for (PhysicalDatabaseInstance instance:existingInstances) {
				if (instance.getClass().equals(this.getClass())) {
					// this is for example MySql and the instance is also MySql
					this.connection = ((SqlDatabaseInstance)instance).connection;
					break;
				}
			}
			if (this.connection == null) {
				this.connection = createConnection(parameters);
			}
		}catch(Throwable t) {
			throw new DatabaseException("Exception while creating connection!" + parameters, t);
		}
	}

	private static SQLDatabaseConnection createConnection(DatabaseParameters parameters) throws SQLException {
		return new SQLDatabaseConnection(parameters); 
	}

	@Override
	protected void closeConnections(boolean dropDatabase) {
		connection.close();
	}

	@Override
	protected void initialiseDatabaseForSchema(Schema schema) {
	}

	@Override
	protected void dropDatabase() {
	}

	@Override
	protected Collection<Atom> addFacts(Collection<Atom> facts) {
		return null;
	}

	@Override
	protected void deleteFacts(Collection<Atom> facts) {

	}

	@Override
	protected Collection<Atom> getFacts() {
		return new ArrayList<Atom>();
	}

	@Override
	protected Collection<Atom> getCachedFacts() {
		return new ArrayList<Atom>();
	}

	@Override
	protected Collection<Atom> getFactsFromPhysicalDatabase() {
		return new ArrayList<Atom>();
	}

	@Override
	protected List<Match> answerQueries(List<PhysicalQuery> queries) {
		List<Match> ret = new ArrayList<>();
		for (PhysicalQuery q:queries) {
			
			try {
				ret.addAll(connection.executeQuery((SQLQuery)q));
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ret;
	}

	@Override
	protected int executeUpdates(List<PhysicalDatabaseCommand> update) {
		return 0;
	}

}
