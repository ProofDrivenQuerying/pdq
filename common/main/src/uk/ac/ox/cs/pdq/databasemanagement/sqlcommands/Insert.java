package uk.ac.ox.cs.pdq.databasemanagement.sqlcommands;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;

public class Insert extends Command {

	/**
	 * Terms of the fact that needs to be stored.
	 */
	private Term[] terms;
	/**
	 * Attributes of the terms above.
	 */
	private Attribute[] attributes;
	private String tableName;
	private final String IGNORE = "{IGNORE}";
	public Insert(Atom fact, Schema schema) {
		this.terms = fact.getTerms();
		tableName = fact.getPredicate().getName();
		attributes = schema.getRelation(tableName).getAttributes();
		replaceTagsMySql.put(IGNORE, "IGNORE");
		replaceTagsDerby.put(IGNORE, "");
		replaceTagsPostgres.put(IGNORE, "");

		String insertInto = "INSERT " + IGNORE + " INTO " + DATABASENAME + "." + fact.getPredicate().getName() + " " + "VALUES ( ";
		for (int termIndex = 0; termIndex < terms.length; ++termIndex) {
			Term term = terms[termIndex];
			if (!term.isVariable()) {
				insertInto += convertTermToSQLString(attributes[termIndex], term);
			}
			if (termIndex < fact.getNumberOfTerms() - 1)
				insertInto += ",";
		}
		insertInto += ")";
		this.statements.add(insertInto);
	}
	
	protected static String convertTermToSQLString(Attribute a, Term term) {
		String termInSqlString = ""; 
		if (!term.isVariable()) {
			if (a.getType() == String.class && term instanceof TypedConstant /*&& !"DatabaseInstanceID".equals(a.getName()) && !"FactId".equals(a.getName())*/)
				termInSqlString += "'" +  ((TypedConstant)term).serializeToString() + "'";
			else if (String.class.isAssignableFrom((Class<?>) a.getType()))
				termInSqlString += "'" +  term + "'";
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

}
