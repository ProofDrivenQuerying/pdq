package uk.ac.ox.cs.pdq.reasoningdatabase.sqlcommands;

import java.util.List;

import com.google.common.base.Joiner;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Term;

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
	public InsertSelect(Relation targetRelation, List<Term> insertVars, ConjunctiveQuery cq, Schema schema) throws DatabaseException {
		super(schema,cq);
		//INSERT INTO {DATABASENAME}.ConstantsToAtoms SELECT attribute0 AS Constant, 'A' AS TableName, 0 AS DatabaseInstanceID, A.FactId AS FactId  FROM {DATABASENAME}.A,{DATABASENAME}.InstanceIdMapping WHERE A.FactId=InstanceIdMapping.FactId AND InstanceIdMapping.DatabaseInstanceID = 0
		this.targetRelation = targetRelation;
		
		String stmt = "INSERT INTO " + Command.DATABASENAME + "." + this.targetRelation.getName() + " SELECT ";
		for (int i = 0; i < targetRelation.getArity(); i++) {
			if (i>0)
				stmt+= ", ";
			if (!insertVars.get(i).isVariable()) {
				stmt+= "'" + insertVars.get(i) + "' AS " + targetRelation.getAttribute(i).getName() + " ";
			} else {
				stmt+= getTableNameFor(insertVars.get(i),cq) + "." + getAttributeNameFor(insertVars.get(i),cq,schema) + " AS " + targetRelation.getAttribute(i).getName() + " ";
			}
		}
		stmt+= " FROM " + Joiner.on(",").join(fromTableName);
		if (!whereConditions.isEmpty())
			stmt += " WHERE " + Joiner.on(" AND ").join(whereConditions);
		statements.clear();
		statements.add(stmt);
	}

	private String getAttributeNameFor(Term term, ConjunctiveQuery cq, Schema s) {
		for (Atom a: cq.getAtoms()) {
			for (int i = 0; i < a.getTerms().length; i++) {
				if (term.equals(a.getTerm(i)))
					return s.getRelation(a.getPredicate().getName()).getAttribute(i).getName();
			}
		}
		return null;
	}
	private String getTableNameFor(Term term, ConjunctiveQuery cq) {
		for (Atom a: cq.getAtoms()) {
			for (int i = 0; i < a.getTerms().length; i++) {
				if (term.equals(a.getTerm(i))) {
					for(TableAlias ta:aliases) {
						if (ta.tableName == a.getPredicate().getName())
							return ta.aliasName;
					}
				}
			}
		}
		return null;
	}
	
}
