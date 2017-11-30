package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;

import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;

/**
 * For large amount of data it could be more effective to use the bulk insert
 * function of the Database provider. It is not implemented in Derby, so this
 * class will map itself to a list a single insert commands when used with
 * Derby.
 * 
 * @author Gabor
 *
 */
public class BulkInsert extends Command {
	/**
	 * Key for the "IGNORE" keyword that MySQL uses.
	 */
	private final String IGNORE = "{IGNORE}";
	/**
	 * Facts to be inserted.
	 */
	private Collection<Atom> facts;
	/**
	 * Schema is needed to map attribute types. (the Atoms only know the predicates
	 * and predicates do not have proper attributes with types.
	 */
	private Schema schema;

	/**
	 * Constructs a single SQL statement that inserts a list of records (facts) into
	 * a database table. In case there are facts for multiple relations it will
	 * create one bulk insert for each relation.
	 * 
	 * @param facts
	 *            to store.
	 * @param schema
	 *            for attribute types.
	 * @throws DatabaseException 
	 */
	public BulkInsert(Collection<Atom> facts, Schema schema) throws DatabaseException {
		this.facts = facts;
		this.schema = schema;
		replaceTagsMySql.put(IGNORE, "IGNORE");
		replaceTagsDerby.put(IGNORE, "");
		replaceTagsPostgres.put(IGNORE, "");
		// Group facts by relation.
		Map<Predicate, List<Atom>> groupedFacts = new HashMap<>();
		for (Atom a : facts) {
			if (groupedFacts.containsKey(a.getPredicate())) {
				groupedFacts.get(a.getPredicate()).add(a);
			} else {
				List<Atom> newList = new ArrayList<>();
				newList.add(a);
				groupedFacts.put(a.getPredicate(), newList);
			}
		}

		// create a single insert for each relation group.
		for (Predicate p : groupedFacts.keySet()) {
			// loop over all groups
			String tableName = p.getName();
			Attribute[] attributes = schema.getRelation(tableName).getAttributes();
			String insertInto = "INSERT " + IGNORE + " INTO " + DATABASENAME + "." + tableName + " " + "VALUES ";
			List<String> values = new ArrayList<String>();
			for (Atom a : groupedFacts.get(p)) {
				// loop over all tuples belonging to this table
				String valueSetString = "(";
				for (int termIndex = 0; termIndex < a.getTerms().length; ++termIndex) {
					Term term = a.getTerms()[termIndex];
					if (!term.isVariable()) {
						valueSetString += convertTermToSQLString(attributes[termIndex], term);
					}
					if (termIndex < a.getNumberOfTerms() - 1)
						valueSetString += ",";
				}
				valueSetString += ")";
				values.add(valueSetString);
			}
			insertInto += Joiner.on(",\n").join(values) + ";";
			this.statements.add(insertInto);
		}
	}

	/*
	 * Derby does not support bulk inserts, so we need to create independent
	 * inserts.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.Command#toDerbyStatement(java.
	 * lang.String, uk.ac.ox.cs.pdq.db.Schema)
	 */
	@Override
	public List<String> toDerbyStatement(String databaseName) throws DatabaseException {
		List<String> result = new ArrayList<>();
		for (Atom fact : facts) {
			result.addAll(new Insert(fact, schema).toDerbyStatement(databaseName));
		}
		return result;
	}

}
