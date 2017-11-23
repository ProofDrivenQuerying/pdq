package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;

/**
 * @author Gabor
 *
 */
public class BulkInsert extends Command {
	private final String IGNORE = "{IGNORE}";
	private Collection<Atom> facts;
	private Schema schema;

	public BulkInsert(Collection<Atom> facts, Schema schema) {
		this.facts = facts;
		this.schema = schema;
		replaceTagsMySql.put(IGNORE, "IGNORE");
		replaceTagsDerby.put(IGNORE, "");
		replaceTagsPostgres.put(IGNORE, "");
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

	protected static String convertTermToSQLString(Attribute a, Term term) {
		String termInSqlString = "";
		if (!term.isVariable()) {
			if (a.getType() == String.class && term instanceof TypedConstant /*
																				 * && !"DatabaseInstanceID".equals(a.getName()) && !"FactId".equals(a.getName())
																				 */)
				termInSqlString += "'" + ((TypedConstant) term).serializeToString() + "'";
			else if (String.class.isAssignableFrom((Class<?>) a.getType()))
				termInSqlString += "'" + term + "'";
			else if (Integer.class.isAssignableFrom((Class<?>) a.getType()))
				termInSqlString += term;
			else if (Double.class.isAssignableFrom((Class<?>) a.getType()))
				termInSqlString += term;
			else if (Float.class.isAssignableFrom((Class<?>) a.getType()))
				termInSqlString += term;
		} else
			throw new RuntimeException("Unsupported type");
		return termInSqlString;
	}
	/* 
	 * Derby does not support bulk inserts, so we need to create independent inserts.
	 * 
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.Command#toDerbyStatement(java.lang.String, uk.ac.ox.cs.pdq.db.Schema)
	 */
	@Override
	public List<String> toDerbyStatement(String databaseName) {
		List<String> result = new ArrayList<>();
		for (Atom fact: facts) {
			result.addAll(new Insert(fact,schema).toDerbyStatement(databaseName));
		}
		return result;
	}

}
