package uk.ac.ox.cs.pdq.reasoningdatabase.sqlcommands;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;

/**
 * This class represents an "INSERT INTO [TableName] SELECT ..." SQL command. The select part is a normal CQ, and we need the Relation where to insert the results.
 * 
 * @author Gabor
 *
 */
public class InsertSelect extends BasicSelect {

	private Relation targetRelation;

	/**
	 * Default constructor is only protected since it shouldn't be used externally,
	 * it is only needed for extending this class.
	 */
	protected InsertSelect() {
	}

	/**
	 * Creates a select based on a CQ.
	 * 
	 * @param schema
	 *            - needed for the attribute types.
	 * @param cq
	 * @throws DatabaseException
	 */
	public InsertSelect(Relation targetRelation, ConjunctiveQuery cq, Schema schema) throws DatabaseException {
		super(schema,cq);
		this.targetRelation = targetRelation;
		String selectOnly = statements.get(0);
		statements.clear();
		statements.add("INSERT INTO " + Command.DATABASENAME + "." + this.targetRelation.getName() + " " + selectOnly);
	}
	
}
