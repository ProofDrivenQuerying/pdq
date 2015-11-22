
package uk.ac.ox.cs.pdq.reasoning.homomorphism;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.BiMap;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager.DBRelation;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint.TopKConstraint;

/**
 * Builds queries for detecting homomorphisms in MySQL
 *
 * @author Efthymia Tsamoura
 *
 */
public class MySQLStatementBuilder extends SQLStatementBuilder {

	public MySQLStatementBuilder() {
		super();
	}
	
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
		return result;
	}
	
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
	 * @return MySQLHomomorphismStatementBuilder
	 */
	@Override
	public MySQLStatementBuilder clone() {
		return new MySQLStatementBuilder(this.cleanMap);
	}

	@Override
	public String encodeName(String name) {
		return super.encodeName(name);
	}

	/**
	 * @param relation the table to create
	 * @return a SQL statement that creates the fact table of the given relation
	 */
	protected String createTableStatement(Relation relation) {
		StringBuilder result = new StringBuilder();
		result.append("CREATE TABLE  ").append(this.encodeName(relation.getName())).append('(');
		for (int it = 0; it < relation.getAttributes().size(); ++it) {
			result.append(' ').append(relation.getAttributes().get(it).getName());
			if(relation.getAttribute(it).getType().toString().contains("java.lang.String")) {
				result.append(" VARCHAR(500),");
			}
			else if(relation.getAttribute(it).getType().toString().contains("java.lang.Integer")) {
				result.append(" int,");
			}
			else {
				throw new java.lang.IllegalArgumentException();
			}
		}
		result.append(" PRIMARY KEY ").append("(").append("Fact").append(")");
		result.append(')');
		return result.toString();
	}
	
	/**
	 * @param facts
	 * @return insert statements that add the input fact to the fact database.
	 */
	@Override
	protected Collection<String> makeInserts(Collection<? extends Predicate> facts, Map<String, DBRelation> dbrelations) {
		Collection<String> result = new LinkedList<>();
		for (Predicate fact : facts) {
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
		return result;
	}
	
	/**
	 * @param facts
	 * @return delete statements that delete the input facts from the fact database.
	 */
	@Override
	protected Collection<String> makeDeletes(Collection<? extends Predicate> facts, Map<String, DBRelation> aliases) {
		Collection<String> result = new LinkedList<>();
		for (Predicate fact : facts) {
			Relation alias = aliases.get(fact.getName());
			String delete = "DELETE FROM " + this.encodeName(alias.getName()) + " " + "WHERE ";
			Attribute attribute = alias.getAttributes().get(alias.getAttributes().size()-1);
			delete += attribute.getName() + "=" + fact.getId();
			result.add(delete);
		}
		return result;
	}
}

