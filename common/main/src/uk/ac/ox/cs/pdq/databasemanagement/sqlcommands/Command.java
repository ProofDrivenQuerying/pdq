package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.db.Schema;

public class Command implements DerbyStatement, MySqlStatement, PostgresStatement {
	public static final String DATABASENAME = "{DATABASENAME}";
	protected List<String> statements = null;
	
	public Command() {
		this.statements = new ArrayList<>(); 
	}
	
	public Command(String command) {
		this.statements = new ArrayList<>(); 
		this.statements.add(command);
	}

	@Override
	public String toString() {
		return "" + statements;
	}
	
	@Override
	public List<String> toPostgresStatement(String databaseName, Schema schema) {
		return replaceTags(statements,DATABASENAME,databaseName);
	}

	@Override
	public List<String> toMySqlStatement(String databaseName, Schema schema) {
		return replaceTags(statements,DATABASENAME,databaseName);
	}

	@Override
	public List<String> toDerbyStatement(String databaseName, Schema schema) {
		return replaceTags(statements,DATABASENAME,databaseName);
	}

	protected List<String> replaceTags(List<String> commands, String tagName, String newValue) {
		List<String> results = new ArrayList<>();
		for (String command:commands) {
			results.add(command.replaceAll(tagName, newValue));
		}
		return results;
	}

}
