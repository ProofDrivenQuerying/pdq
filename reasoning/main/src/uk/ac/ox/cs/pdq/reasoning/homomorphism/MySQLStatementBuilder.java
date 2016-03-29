
package uk.ac.ox.cs.pdq.reasoning.homomorphism;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.collect.BiMap;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager.DBRelation;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint.TopKConstraint;

// TODO: Auto-generated Javadoc
/**
 * Builds queries for detecting homomorphisms in MySQL.
 *
 * @author Efthymia Tsamoura
 */
public class MySQLStatementBuilder extends SQLStatementBuilder {

	/** The log. */
	private static Logger log = Logger.getLogger(MySQLStatementBuilder.class);
	
	/**
	 * Instantiates a new my sql statement builder.
	 */
	public MySQLStatementBuilder() {
		super();
	}
	
	/**
	 * Instantiates a new my sql statement builder.
	 *
	 * @param cleanMap the clean map
	 */
	protected MySQLStatementBuilder(BiMap<String, String> cleanMap) {
		super(cleanMap);
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.homomorphism.AbstractHomomorphismStatementBuilder#setupStatements(java.lang.String)
	 */
	@Override
	public Collection<String> setupStatements(String databaseName) {
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
	public Collection<String> cleanupStatements(String databaseName) {
		Collection<String> result = new LinkedList<>();
		result.add("DROP DATABASE " + databaseName);
		log.trace(result);
		return result;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.homomorphism.SQLStatementBuilder#translateLimitConstraints(uk.ac.ox.cs.pdq.fol.Evaluatable, uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint[])
	 */
	@Override
	protected String translateLimitConstraints(Evaluatable source, HomomorphismConstraint... constraints) {
		for(HomomorphismConstraint c:constraints) {
			if(c instanceof TopKConstraint) {
				return "LIMIT " + ((TopKConstraint) c).k;
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
		return new MySQLStatementBuilder(this.cleanMap);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.homomorphism.SQLStatementBuilder#encodeName(java.lang.String)
	 */
	@Override
	public String encodeName(String name) {
		return super.encodeName(name);
	}
	
	/**
	 * Make inserts.
	 *
	 * @param facts the facts
	 * @param dbrelations the dbrelations
	 * @return insert statements that add the input fact to the fact database.
	 */
	@Override
	protected Collection<String> makeInserts(Collection<? extends Atom> facts, Map<String, DBRelation> dbrelations) {
		Collection<String> result = new LinkedList<>();
		for (Atom fact : facts) {
			DBRelation rel = dbrelations.get(fact.getName());
			List<Term> terms = fact.getTerms();
			String insertInto = "INSERT IGNORE INTO " + this.encodeName(rel.getName()) + " " + "VALUES ( ";
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
}

