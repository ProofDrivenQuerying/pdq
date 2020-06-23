// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.reasoningdatabase.sqlcommands;

import java.util.List;

import uk.ac.ox.cs.pdq.db.Schema;

/**
 * Creates an empty schema with the given databaseName.
 * 
 * @author Gabor
 *
 */
public class CreateDatabase extends DropDatabase {

	/**
	 * Creates a schema. In case of Postgres and MySql it will drop the same schema
	 * if it exists. 
	 */
	public CreateDatabase(Schema schema) {
		super(schema);
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
}
