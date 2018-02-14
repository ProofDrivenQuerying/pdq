package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import java.util.List;

import uk.ac.ox.cs.pdq.db.Schema;

/**
 * Drops the existing database recursively.
 * 
 * @author Gabor
 *
 */
public class DropDatabase extends Command {
	protected Schema schema;

	/**
	 * Constructor, empty since we will implement this command in a language to
	 * language way, because the way to create and drop databases are usually
	 * different in each dialect.
	 */
	public DropDatabase(Schema schema) {
		super();
		this.schema = schema;
	}

	@Override
	public List<String> toPostgresStatement(String databaseName) {
		statements.clear();
		statements.add("DROP SCHEMA IF EXISTS " + databaseName + " CASCADE");
		return statements;
	}

	@Override
	public List<String> toMySqlStatement(String databaseName) {
		statements.clear();
		statements.add("DROP SCHEMA IF EXISTS " + databaseName);
		return statements;
	}
}
