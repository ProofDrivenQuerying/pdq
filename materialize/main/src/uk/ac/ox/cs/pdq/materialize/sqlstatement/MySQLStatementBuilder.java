
package uk.ac.ox.cs.pdq.materialize.sqlstatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.materialize.homomorphism.DatabaseRelation;
import uk.ac.ox.cs.pdq.materialize.homomorphism.HomomorphismProperty;
import uk.ac.ox.cs.pdq.materialize.homomorphism.HomomorphismProperty.TopKProperty;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Joiner;

// TODO: Auto-generated Javadoc
/**
 * Builds queries for detecting homomorphisms in MySQL.
 *
 * @author Efthymia Tsamoura
 */
public class MySQLStatementBuilder extends SQLStatementBuilder {

	/** The log. */
	private static Logger log = Logger.getLogger(MySQLStatementBuilder.class);

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.homomorphism.AbstractHomomorphismStatementBuilder#setupStatements(java.lang.String)
	 */
	@Override
	public Collection<String> createDatabaseStatements(String databaseName) {
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

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.homomorphism.SQLStatementBuilder#translateLimitConstraints(uk.ac.ox.cs.pdq.fol.Evaluatable, uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint[])
	 */
	@Override
	protected String translateLimitConstraints(Evaluatable source, HomomorphismProperty... constraints) {
		for(HomomorphismProperty c:constraints) {
			if(c instanceof TopKProperty) {
				return "LIMIT " + ((TopKProperty) c).k;
			}
		}
		return null;
	}

	/**
	 * Clone.
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
	public Collection<String> createInsertStatements(Collection<? extends Atom> facts, Map<String, DatabaseRelation> toDatabaseTables) {
		Collection<String> result = new LinkedList<>();
		for (Atom fact:facts) {
			DatabaseRelation rel = toDatabaseTables.get(fact.getName());
			List<Term> terms = fact.getTerms();
			String insertInto = "INSERT IGNORE INTO " + toDatabaseTables.get(rel.getName()).getName() + " " + "VALUES ( ";
			for (Term term : terms) {
				if (!term.isVariable()) {
					insertInto += "'" + term + "'" + ",";
				}
			}
			insertInto += 0 + ",";
			insertInto += fact.getId();
			insertInto += ")";
			result.add(insertInto);
		}
		log.trace(result);
		return result;
	}

	/**
	 * Make inserts.
	 *
	 * @param facts the facts
	 * @param toDatabaseTables the dbrelations
	 * @return insert statements that add the input fact to the fact database.
	 */
	@Override
	public String createBulkInsertStatement(Predicate predicate, Collection<? extends Atom> facts, Map<String, DatabaseRelation> toDatabaseTables) {
		String insertInto = "INSERT IGNORE INTO " + toDatabaseTables.get(predicate.getName()).getName() + "\n" +
				"VALUES" + "\n";
		List<String> tuples = Lists.newArrayList();
		for (Atom fact:facts) {
			String tuple = "(";
			List<Term> terms = fact.getTerms();
			for (Term term : terms) {
				if (!term.isVariable()) {
					tuple += "'" + term + "'" + ",";
				}
			}
			tuple += fact.getId();
			tuple += ")";
			tuples.add(tuple);
		}
		insertInto += Joiner.on(",\n").join(tuples) + ";";
		return insertInto;
	}
}

