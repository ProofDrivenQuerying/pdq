package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.db.Schema;

public class DropDatabase extends Command {
	public DropDatabase() {
		super();
	}
	
	@Override
	public List<String> toPostgresStatement(String databaseName, Schema schema) {
		statements.add("DROP SCHEMA IF EXISTS " + databaseName + " CASCADE");
		statements.add("CREATE SCHEMA " + databaseName);
		statements.add("SET search_path TO " + databaseName);
		return statements;
	}

	@Override
	public List<String> toMySqlStatement(String databaseName, Schema schema) {
		statements.add("DROP SCHEMA IF EXISTS " + databaseName);
		statements.add("CREATE DATABASE " + databaseName);
		statements.add("USE " + databaseName);
		return statements;
	}

	@Override
	public List<String> toDerbyStatement(String databaseName, Schema schema) {
		return new ArrayList<>();
	}

}
