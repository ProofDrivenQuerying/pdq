package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import java.util.List;

import uk.ac.ox.cs.pdq.db.Schema;

public interface PostgresStatement {

	public List<String> toPostgresStatement(String databaseName, Schema schema);
}
