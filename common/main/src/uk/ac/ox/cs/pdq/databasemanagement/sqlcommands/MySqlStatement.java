package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import java.util.List;

import uk.ac.ox.cs.pdq.db.Schema;

public interface MySqlStatement {

	public List<String> toMySqlStatement(String databaseName, Schema schema);
}
