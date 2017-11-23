package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import java.util.List;

public interface MySqlStatement {

	public List<String> toMySqlStatement(String databaseName);
}
