// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.reasoningdatabase.sqlcommands;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;

/**
 * This class represents a SQL query. Can be created from a CQ, or a simple
 * relation (when you want to get all data from that relation)
 * 
 * @author Gabor
 *
 */
public class ExplainSelect extends BasicSelect {

	/**
	 * Creates a select based on a CQ.
	 * 
	 * @param schema
	 *            - needed for the attribute types.
	 * @param cq
	 * @throws DatabaseException
	 */
	public ExplainSelect(Schema schema, ConjunctiveQuery cq) throws DatabaseException {
		super(schema,cq);
		String query = statements.get(0);
		statements.clear();
		statements.add("EXPLAIN " + query);
	}
}
