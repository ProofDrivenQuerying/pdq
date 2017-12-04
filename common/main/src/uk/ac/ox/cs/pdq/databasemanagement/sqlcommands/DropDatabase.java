package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import java.util.List;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;

/**
 * Drops the existing database recursively, and re-creates it. It means the
 * empty database will stay there. It is necessary to make sure we will be able
 * to reconnect to it. In case we would not re-create the database immediately
 * then if we loose the connection we won't be able to reconnect. (most database
 * provider only allows remote connection to existing databases)
 * 
 * @author Gabor
 *
 */
public class DropDatabase extends Command {
	private Schema schema;

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
		// drop database (prostgres calls it SCHEMA) and then re- create it and set the
		// new one as default.
		statements.add("DROP SCHEMA IF EXISTS " + databaseName + " CASCADE");
		statements.add("CREATE SCHEMA " + databaseName);
		statements.add("SET search_path TO " + databaseName);
		return statements;
	}

	@Override
	public List<String> toMySqlStatement(String databaseName) {
		// drop database (mySql calls it SCHEMA) and then re- create it and set the new
		// one as default.
		statements.add("DROP SCHEMA IF EXISTS " + databaseName);
		statements.add("CREATE DATABASE " + databaseName);
		statements.add("USE " + databaseName);
		return statements;
	}

	@Override
	public List<String> toDerbyStatement(String databaseName) {
		for (Relation table: schema.getRelations())  
			statements.add("DROP TABLE " + databaseName + "." + table.getName()); 
		return statements;
	}

}
