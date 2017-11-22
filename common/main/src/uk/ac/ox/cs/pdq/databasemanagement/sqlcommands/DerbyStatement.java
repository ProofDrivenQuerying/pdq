package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import java.util.List;

import uk.ac.ox.cs.pdq.db.Schema;

public interface DerbyStatement {

	public List<String> toDerbyStatement(String databaseName, Schema schema);
}
