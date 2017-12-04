package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;

/**
 * Represents an INSERT statement. It is for a single record (fact) bulk insert
 * is implemented as a separate class.
 * 
 * @author Gabor
 *
 */
public class Insert extends Command {

	/**
	 * Terms of the fact that needs to be stored.
	 */
	private Term[] terms;
	/**
	 * Attributes of the terms above.
	 */
	private Attribute[] attributes;
	/**
	 * Name of the table we are inserting to.
	 */
	private String tableName;

	/**
	 * Constructs this Insert statement.
	 * 
	 * @param fact
	 *            the fact to be added.
	 * @param schema
	 *            - for relation name and attribute types.
	 * @throws DatabaseException
	 *             - in case the fact contains something that is not a constant.
	 */
	public Insert(Atom fact, Schema schema) throws DatabaseException {
		// get the terms
		this.terms = fact.getTerms();
		tableName = fact.getPredicate().getName();

		// check if we have the table
		if (schema.getRelation(tableName) == null) {
			throw new DatabaseException("Table name for fact: " + fact + " not found in schema: " + schema);
		}

		// get the attributes
		attributes = schema.getRelation(tableName).getAttributes();

		// build the actual INSERT INTO statement
		String insertInto = "INSERT INTO " + DATABASENAME + "." + fact.getPredicate().getName() + " " + "VALUES ( ";
		for (int termIndex = 0; termIndex < terms.length; ++termIndex) {
			Term term = terms[termIndex];
			if (!term.isVariable()) {
				// convert the constant term to a string
				insertInto += convertTermToSQLString(attributes[termIndex], term);
			} else {
				// Variables are not allowed to be stored.
				throw new DatabaseException("It is not allowed to insert Variables to the database: " + fact);
			}
			if (termIndex < fact.getNumberOfTerms() - 1)
				insertInto += ",";
		}
		insertInto += ")";
		this.statements.add(insertInto);
	}
}
