// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.reasoningdatabase.sqlcommands;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.fol.Atom;

/**
 * Represents a DELETE FROM sql statement, can be used to delete a single fact.
 * 
 * @author Gabor
 *
 */
public class Delete extends Command {
	public final String SEMICOLON = "{SEMICOLON}";

	/**
	 * Constructor.
	 * 
	 * @param factToDelete
	 *            fact to delete
	 * @param schema
	 *            - schema is needed to get the relation (the fact contains a
	 *            predicate only that might not be a relation)
	 * @throws DatabaseException
	 *             in case the input fact contains some error. For example it is not
	 *             connected to the given schema by having an unknown predicate.
	 */
	public Delete(Atom factToDelete, Schema schema) throws DatabaseException {
		super();
		// add dialect specific mappings.
		replaceTagsMySql.put(SEMICOLON, ";");
		replaceTagsPostgres.put(SEMICOLON, ";");

		// add the actual DELETE FROM statement.
		statements.add(createDeleteStatement(factToDelete, schema) + SEMICOLON);
	}

	/**
	 * Creates a delete form statement string
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	protected String createDeleteStatement(Atom fact, Schema schema) throws DatabaseException {
		if (fact == null || schema == null)
			throw new DatabaseException("Cant delete unset fact or from an unset schema. Fact: " + fact + ", schema: " + schema);
		// header
		String deleteFrom = "DELETE FROM " + DATABASENAME + "." + fact.getPredicate().getName() + " WHERE ";
		Relation r = schema.getRelation(fact.getPredicate().getName());

		if (r == null)
			throw new DatabaseException("Fact : " + fact + " doesn't belong to schema " + schema);

		if (r.getAttributes().length != fact.getTerms().length)
			throw new DatabaseException("Fact have different number of terms then the attributes of the relation: " + fact + ", relation " + r);

		int index = 0;
		boolean firstAdded=false;
		
		// condition
		for (Attribute a : r.getAttributes()) {
			if (fact.getTerm(index).isVariable()) {
				index++;
			} else {
				if (firstAdded)
					deleteFrom += " AND ";
				deleteFrom += a.getName() + " = " + convertTermToSQLString(a, fact.getTerm(index)) + " ";
				firstAdded=true;
				index++;
			}
		}
		deleteFrom += "\n";
		return deleteFrom;
	}
}
