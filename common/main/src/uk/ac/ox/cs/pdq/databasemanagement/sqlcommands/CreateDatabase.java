package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

/**
 * To create a new Database it means we have to drop the already existing one.
 * Also to avoid synchronisation issues dropping a database will create an empty
 * one immediately, so this class can simply extend DropDatabase.
 * 
 * @author Gabor
 *
 */
public class CreateDatabase extends DropDatabase {

	public CreateDatabase() {
		super();
	}

}
