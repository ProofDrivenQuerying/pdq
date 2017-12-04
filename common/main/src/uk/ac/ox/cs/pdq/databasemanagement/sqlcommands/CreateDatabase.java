package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.db.Schema;

/**
 * To create a new Database it means we have to drop the already existing one.
 * Also to avoid synchronisation issues dropping a database will create an empty
 * one immediately, so this class can simply extend DropDatabase.
 * 
 * @author Gabor
 *
 */
public class CreateDatabase extends DropDatabase {

	/**
	 * The create and Drop database is the same since we can't risk dropping a
	 * database and not re-creating it immediately (in such case the database have
	 * to be re-created manually, since most database provider does not allow remote
	 * connection to a database that does not exists.)
	 */
	public CreateDatabase(Schema schema) {
		super(schema);
	}
	@Override
	public List<String> toDerbyStatement(String databaseName) {
		// derby have to be empty.
		return new ArrayList<String>();
	}

}
