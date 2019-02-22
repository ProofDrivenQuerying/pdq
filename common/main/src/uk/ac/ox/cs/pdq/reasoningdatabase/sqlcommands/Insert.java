package uk.ac.ox.cs.pdq.reasoningdatabase.sqlcommands;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
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
		if (fact == null || schema == null)
			throw new DatabaseException("Cant delete unset fact or from an unset schema. Fact: " + fact + ", schema: " + schema);
		// header
		Relation r = schema.getRelation(fact.getPredicate().getName());

		if (r == null)
			throw new DatabaseException("Fact : " + fact + " doesn't belong to schema " + schema);

		// get the attributes
		attributes = r.getAttributes();
		if (attributes.length != fact.getTerms().length)
			throw new DatabaseException("Fact have different number of terms then the attributes of the relation: " + fact + ", relation " + r);

		// get the terms
		this.terms = fact.getTerms();

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
