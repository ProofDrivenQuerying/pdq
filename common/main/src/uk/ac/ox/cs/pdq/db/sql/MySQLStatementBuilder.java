
package uk.ac.ox.cs.pdq.db.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Assert;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;

import com.google.common.base.Joiner;

// TODO: Auto-generated Javadoc
/**
 * Builds queries for detecting homomorphisms in MySQL.
 *
 * @author Efthymia Tsamoura
 */
public class MySQLStatementBuilder extends SQLStatementBuilder {

	private static Logger log = Logger.getLogger(MySQLStatementBuilder.class);

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.homomorphism.AbstractHomomorphismStatementBuilder#setupStatements(java.lang.String)
	 */
	@Override
	public Collection<String> createDatabaseStatements(String databaseName) {
		this.databaseName = databaseName;
		Collection<String> result = new LinkedList<>();
		result.add("DROP DATABASE IF EXISTS " + databaseName);
		result.add("CREATE DATABASE " + databaseName);
		result.add("USE " + databaseName);
		log.trace(result);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.homomorphism.AbstractHomomorphismStatementBuilder#cleanupStatements(java.lang.String)
	 */
	@Override
	public Collection<String> createDropStatements(String databaseName) {
		Collection<String> result = new LinkedList<>();
		result.add("DROP DATABASE " + databaseName);
		log.trace(result);
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
	 * Make inserts.
	 *
	 * @param facts the facts
	 * @param toDatabaseTables the dbrelations
	 * @return insert statements that add the input fact to the fact database.
	 */
	@Override
	public Collection<String> createInsertStatements(Collection<Atom> facts, Map<String, Relation> relationNamesToDatabaseTables) {
		Collection<String> result = new LinkedList<>();
		for (Atom fact:facts) {
			Assert.assertTrue(fact.getPredicate() instanceof Relation);
			Relation relation = (Relation) fact.getPredicate();
			String insertInto = "INSERT IGNORE INTO " + databaseName+"." + fact.getPredicate().getName() + " " + "VALUES ( ";
			for (int termIndex = 0; termIndex < fact.getNumberOfTerms(); ++termIndex) {
				Term term = fact.getTerm(termIndex);
				if (!term.isVariable()) 
					if (String.class.isAssignableFrom((Class<?>) relation.getAttribute(termIndex).getType()))
						insertInto += "'" + term + "'";
					else if (Integer.class.isAssignableFrom((Class<?>) relation.getAttribute(termIndex).getType()))
						insertInto +=  term;
					else if (Double.class.isAssignableFrom((Class<?>) relation.getAttribute(termIndex).getType()))
						insertInto +=  term;
					else if (Float.class.isAssignableFrom((Class<?>) relation.getAttribute(termIndex).getType()))
						insertInto +=  term;
					else 
						throw new RuntimeException("Unsupported type");
				if(termIndex < fact.getNumberOfTerms() -1)
					insertInto +=  ",";
			}
			insertInto += ")";
			result.add(insertInto);
		}
		log.trace(result);
		return result;
	}

	/**
	 * TOCOMMENT: EXPLAIN DIFFERENCE FROM PRIOR
	 *
	 * @param facts the facts
	 * @param toDatabaseTables the dbrelations
	 * @return insert statements that add the input fact to the fact database.
	 */
	@Override
	public String createBulkInsertStatement(Predicate predicate, Collection<Atom> facts) {
		
		String insertInto = "INSERT IGNORE INTO " + databaseName+"." + predicate.getName() + "\n" + "VALUES" + "\n";
		List<String> tuples = new ArrayList<String>();
		for (Atom fact:facts) {
			String tuple = "(";
			for (int termIndex = 0; termIndex < fact.getNumberOfTerms(); ++termIndex) {
				Term term = fact.getTerm(termIndex);
				if (!term.isVariable()) 
					tuple += "'" + term + "'";
				if(termIndex < fact.getNumberOfTerms() - 1)
					tuple += ",";
			}
			tuple += ")";
			tuples.add(tuple);
		}
		insertInto += Joiner.on(",\n").join(tuples) + ";";
		return insertInto;
	}
	
	
	@Override
	public String createBulkDeleteStatement(Predicate predicate, Collection<Atom> facts, Map<String, Relation> relationNamesToDatabaseTables) {
		return super.createBulkDeleteStatement(predicate, facts, relationNamesToDatabaseTables)+";";
	}
}

