package uk.ac.ox.cs.pdq.reasoning.homomorphism;

import java.util.Collection;
import java.util.LinkedList;

import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint.TopK;

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
			if(c instanceof TopK) {
				return "LIMIT " + ((TopK) c).k;
			}
		}
		return null;
	}
	
	/**
	 * @return MySQLHomomorphismStatementBuilder
	 */
	@Override
	public MySQLStatementBuilder clone() {
		return new MySQLStatementBuilder();
	}

	@Override
	public String encodeName(String name) {
		return super.encodeName(name);
	}
}
