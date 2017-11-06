package uk.ac.ox.cs.pdq.data.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Joiner;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.PrimaryKey;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.db.sql.MySQLStatementBuilder;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;

public class MySqlDatabaseInstance extends SqlDatabaseInstance {

	public MySqlDatabaseInstance(DatabaseParameters parameters) {
		super(parameters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ox.cs.pdq.homomorphism.AbstractHomomorphismStatementBuilder#
	 * setupStatements(java.lang.String)
	 */
	@Override
	public Collection<String> createDatabaseStatements(String databaseName) {
		Collection<String> result = new LinkedList<>();
		result.add("DROP DATABASE IF EXISTS " + databaseName);
		result.add("CREATE DATABASE " + databaseName);
		result.add("USE " + databaseName);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ox.cs.pdq.homomorphism.AbstractHomomorphismStatementBuilder#
	 * cleanupStatements(java.lang.String)
	 */
	@Override
	public Collection<String> createDropStatements(String databaseName) {
		Collection<String> result = new LinkedList<>();
		if (!"pdq".equalsIgnoreCase(databaseName))
			// the default database that we connect to should never be dropped
			result.add("DROP DATABASE " + databaseName);
		return result;
	}

	/**
	 *
	 * @return MySQLHomomorphismStatementBuilder
	 */
	@Override
	public MySQLStatementBuilder clone() {
		return new MySQLStatementBuilder();
	}

	/**
	 * Creates a single "insert into XYZ" statement with all the terms of each fact.
	 * Make inserts.
	 *
	 * @param facts
	 *            the facts
	 * @param toDatabaseTables
	 *            the dbrelations
	 * @return insert statements that add the input fact to the fact database.
	 */
	@Override
	public Collection<String> createInsertStatements(Collection<Atom> facts) {
		Collection<String> result = new LinkedList<>();
		for (Atom fact : facts) {
			// Assert.assertTrue(fact.getPredicate() instanceof Relation);
			Relation relation = this.schema.getRelation(fact.getPredicate().getName());
			String insertInto = "INSERT IGNORE INTO " + databaseParameters.getDatabaseName() + "." + fact.getPredicate().getName() + " " + "VALUES ( ";
			for (int termIndex = 0; termIndex < fact.getNumberOfTerms(); ++termIndex) {
				Term term = fact.getTerm(termIndex);
				if (!term.isVariable()) {
					insertInto += convertTermToSQLString(relation.getAttribute(termIndex), term);
				}
				if (termIndex < fact.getNumberOfTerms() - 1)
					insertInto += ",";
			}
			insertInto += ")";
			result.add(insertInto);
		}
		return result;
	}

	/**
	 * Creates a single "insert into XYZ" statement with all the facts.
	 * 
	 * @param facts
	 *            the facts
	 * @param toDatabaseTables
	 *            the dbrelations
	 * @return insert statements that add the input fact to the fact database.
	 */
	@Override
	public String createBulkInsertStatement(Predicate predicate, Collection<Atom> facts) {

		String insertInto = "INSERT IGNORE INTO " + databaseParameters.getDatabaseName() + "." + predicate.getName() + "\n" + "VALUES" + "\n";
		List<String> tuples = new ArrayList<String>();
		for (Atom fact : facts) {
			String tuple = "(";
			for (int termIndex = 0; termIndex < fact.getNumberOfTerms(); ++termIndex) {
				Term term = fact.getTerm(termIndex);
				if (term instanceof TypedConstant == termIndex < fact.getNumberOfTerms() - 1) {
					tuple += "'" + ((TypedConstant) term).serializeToString() + "'";
				} else if (!term.isVariable())
					tuple += "'" + term + "'";
				if (termIndex < fact.getNumberOfTerms() - 1)
					tuple += ",";
			}
			tuple += ")";
			tuples.add(tuple);
		}
		insertInto += Joiner.on(",\n").join(tuples) + ";";
		return insertInto;
	}

	@Override
	public String createDeleteStatement(Atom fact) {
		return super.createDeleteStatement(fact) + ";";
	}

	/**
	 * Creates the table statement.
	 *
	 * @param relation
	 *            the table to create
	 * @return a SQL statement that creates the fact table of the given relation
	 */
	public String createTableStatement(Relation relation) {
		StringBuilder result = new StringBuilder();
		result.append("CREATE TABLE " + databaseParameters.getDatabaseName() + ".").append(relation.getName()).append('(');
		for (int attributeIndex = 0; attributeIndex < relation.getArity(); ++attributeIndex) {
			Attribute attribute = relation.getAttribute(attributeIndex);
			result.append(attribute.getName());
			if (String.class.isAssignableFrom((Class<?>) attribute.getType()))
				result.append(" TEXT");
			else if (Integer.class.isAssignableFrom((Class<?>) attribute.getType()))
				result.append(" INT");
			else if (Double.class.isAssignableFrom((Class<?>) attribute.getType()))
				result.append(" DOUBLE");
			else if (Float.class.isAssignableFrom((Class<?>) attribute.getType()))
				result.append(" FLOAT");
			else
				throw new RuntimeException("Unsupported type");
			if (attributeIndex < relation.getArity() - 1)
				result.append(",");
		}
		String keyAttributes = null;
		PrimaryKey pk = relation.getKey();
		if (pk != null) {
			for (Attribute a : pk.getAttributes()) {
				if (keyAttributes == null) {
					keyAttributes = "";
				} else {
					keyAttributes += ",";
				}
				keyAttributes += a.getName();
				keyAttributes += " ";
			}
			result.append(" PRIMARY KEY ").append("(").append(keyAttributes).append(")");
		}
		result.append(')');
		return result.toString();
	}

}
