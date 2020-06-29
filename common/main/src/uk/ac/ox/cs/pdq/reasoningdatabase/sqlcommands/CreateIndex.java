// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.reasoningdatabase.sqlcommands;

import com.google.common.base.Joiner;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;

/**
 * This class represents a CREATE INDEX sql command.
 * 
 * @author Gabor
 *
 */
public class CreateIndex extends Command {
	/**
	 * Constructs the indices for given relations.
	 * @param relations
	 * @throws DatabaseException in case it contains not allowed table or attribute names.
	 */
	public CreateIndex(Relation[] relations) throws DatabaseException {
		super();
		for (Relation r : relations) {
			if (r.getIndexedAttributes().length > 0)
				statements.add(createIndexStatement(r));
		}
	}
	private String createIndexStatement(Relation r) {
		return "CREATE INDEX " + r.getName() + "_Index" + " ON " + DATABASENAME + "." + r.getName() + " ("+Joiner.on(",").join(r.getIndexedAttributes())+");";		
	}
}
