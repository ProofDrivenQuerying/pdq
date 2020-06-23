// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.reasoningdatabase.sqlcommands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;

/**
 * Main class of this package. It is the superclass of all database commands. It
 * provides functionality to convert an SQL statement to certain SQL dialect
 * such as Postgres or MySql. Stores the actual statements as well.
 * 
 * @author Gabor
 *
 */
public class Command {
	/**
	 * The actual statements that we need to execute. It is a list since some
	 * commands can use multiple statements, such as Drop Database, create table
	 * (also creating indexes)
	 * 
	 * The statements here can have keywords, one for the database name, and others
	 * that can be set according to different SQL dialects. All mapping will be done
	 * by the toXYZStatement methods.
	 */
	protected List<String> statements = null;
	/**
	 * Keyword for database name. The best way to form a reference to a table is to
	 * use DATABASENAME.TABLENAME, but the database name is unknown until we are
	 * ready to execute the command on a certain database, so it is best to use
	 * {DATABASENAME}.tablename in the prepared Sql statement string, and replace
	 * the keyword just before the execution happens.
	 */
	public static final String DATABASENAME = "{DATABASENAME}";
	/**
	 * Similar to the DATABASENAME keyword, but it contains both keys to replace and
	 * values to be used in case of certain dialect. Only one of these will be used.
	 * This one in case the actual database provider speaks MySQL dialect.
	 */
	protected Map<String, String> replaceTagsMySql = new HashMap<>();
	/**
	 * Same as above but for Postgres dialect.
	 */
	protected Map<String, String> replaceTagsPostgres = new HashMap<>();

	/**
	 * When set to true, it will execute each statement separately and ignore any error.
	 */
	protected boolean ignoreErrors = false;
	
	protected Exception creatorStack = null;
	/**
	 * Constructs an empty command.
	 */
	public Command() {
		this.statements = new ArrayList<>();
		creatorStack = new Exception(this.getClass().getSimpleName() + " creation stack.");
		creatorStack.fillInStackTrace();
	}

	/**
	 * Same as above but adds one statement.
	 * 
	 * @param command
	 *            the statement to add.
	 */
	public Command(String command) {
		this.statements = new ArrayList<>();
		this.statements.add(command);
		creatorStack = new Exception(this.getClass().getSimpleName() + " creation stack.");
		creatorStack.fillInStackTrace();
	}

	/**
	 * Converts this Command object to an SQL statement string, Postgres dialect.
	 * 
	 * @param databaseName
	 *            the database name is a common keyword used to precisely identify
	 *            tables.
	 * @return list of sql statements representing this Command object
	 */
	public List<String> toPostgresStatement(String databaseName) {
		List<String> newStatements = replaceTags(statements, DATABASENAME, databaseName);
		for (String key : replaceTagsPostgres.keySet()) {
			newStatements = replaceTags(newStatements, key, replaceTagsPostgres.get(key));
		}
		return newStatements;
	}

	/**
	 * Converts this Command object to an SQL statement string, MySQL dialect.
	 * 
	 * @param databaseName
	 *            the database name is a common keyword used to precisely identify
	 *            tables.
	 * @return list of sql statements representing this Command object
	 */
	public List<String> toMySqlStatement(String databaseName) {
		List<String> newStatements = replaceTags(statements, DATABASENAME, databaseName);
		for (String key : replaceTagsMySql.keySet()) {
			newStatements = replaceTags(newStatements, key, replaceTagsMySql.get(key));
		}
		return newStatements;
	}
	
	/**
	 * Replaces a single tag with a newValue in every statement.
	 * 
	 * @return
	 */
	protected List<String> replaceTags(List<String> commands, String tagName, String newValue) {
		List<String> results = new ArrayList<>();
		for (String command : commands) {
			results.add(command.replace(tagName, newValue));
		}
		return results;
	}

	/**
	 * This function converts a constant term to a database string. For example the
	 * number 3 will be converted to "3", but the string "apple" will be converted
	 * to "'apple'", and a TypedConstant that should be converted to a string will
	 * be serialised.
	 * 
	 * @param a
	 *            - attribute of the database relation where we want to store the
	 *            term. Could be different then the type of term ( for example we
	 *            can map anything to string )
	 * @param term
	 *            constant term to convert.
	 * @return
	 * @throws DatabaseException 
	 */
	protected static String convertTermToSQLString(Attribute a, Term term) throws DatabaseException {
		String termInSqlString = "";
		if (!term.isVariable()) {

			if (a.getType() == String.class && term instanceof TypedConstant && !"DatabaseInstanceID".equals(a.getName()) && !"FactId".equals(a.getName()))
				termInSqlString += "'" + ((TypedConstant) term).serializeToString() + "'";
			else if (term instanceof UntypedConstant)
				termInSqlString += "'" + term + "'";
			else if (String.class.isAssignableFrom((Class<?>) a.getType()))
				termInSqlString += "'" + term + "'";
			else if (Integer.class.isAssignableFrom((Class<?>) a.getType()))
				termInSqlString += term;
			else if (Double.class.isAssignableFrom((Class<?>) a.getType()))
				termInSqlString += term;
			else if (Float.class.isAssignableFrom((Class<?>) a.getType()))
				termInSqlString += term;
			else if (Long.class.isAssignableFrom((Class<?>) a.getType()))
				termInSqlString += term;
			else
				termInSqlString += term;
		} else
			throw new DatabaseException("Unsupported type");
		return termInSqlString;
	}

	/*
	 * For debugging purpose only.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + statements + ")";
	}

	public boolean isIgnoreErrors() {
		return ignoreErrors;
	}

	public void printCallerStackTrace() {
		creatorStack.printStackTrace();
	}

}
