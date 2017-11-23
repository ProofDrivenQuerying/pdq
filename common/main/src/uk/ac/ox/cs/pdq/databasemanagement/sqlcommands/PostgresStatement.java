package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import java.util.List;

public interface PostgresStatement {

	public List<String> toPostgresStatement(String databaseName);
}
