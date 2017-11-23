package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;

/**
 * @author Gabor
 *
 */
public class Delete extends Command {
	public final String SEMICOLON = "{SEMICOLON}";

	public Delete(Atom factToDelete, Schema schema) {
		super();
		replaceTagsDerby.put(SEMICOLON, ""); // derby doesn't like the semicolon at the end of the line.
		replaceTagsMySql.put(SEMICOLON, ";");
		replaceTagsPostgres.put(SEMICOLON, ";");
		statements.add(createDeleteStatement(factToDelete, schema) + SEMICOLON);
	}

	protected String createDeleteStatement(Atom fact, Schema schema) {
		String deleteFrom = "DELETE FROM " + DATABASENAME + "." + fact.getPredicate().getName() + " WHERE ";
		Relation r = schema.getRelation(fact.getPredicate().getName());
		int index = 0;
		for (Attribute a : r.getAttributes()) {
			if (index != 0)
				deleteFrom += " AND ";
			deleteFrom += a.getName() + " = " + convertTermToSQLString(a, fact.getTerm(index)) + " ";
			index++;
		}
		deleteFrom += "\n";
		return deleteFrom;
	}
}
