package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import java.util.List;

public interface DerbyStatement {

	public List<String> toDerbyStatement(String databaseName);
}
