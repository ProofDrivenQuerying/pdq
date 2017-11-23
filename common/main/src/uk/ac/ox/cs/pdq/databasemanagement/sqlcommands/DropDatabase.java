package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gabor
 *
 */
public class DropDatabase extends Command {
	public DropDatabase() {
		super();
	}
	
	@Override
	public List<String> toPostgresStatement(String databaseName) {
		statements.add("DROP SCHEMA IF EXISTS " + databaseName + " CASCADE");
		statements.add("CREATE SCHEMA " + databaseName);
		statements.add("SET search_path TO " + databaseName);
		return statements;
	}

	@Override
	public List<String> toMySqlStatement(String databaseName) {
		statements.add("DROP SCHEMA IF EXISTS " + databaseName);
		statements.add("CREATE DATABASE " + databaseName);
		statements.add("USE " + databaseName);
		return statements;
	}

	@Override
	public List<String> toDerbyStatement(String databaseName) {
		// derby database does not need to be dropped.
		return new ArrayList<>();
	}

}
