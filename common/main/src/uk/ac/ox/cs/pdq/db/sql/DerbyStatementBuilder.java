package uk.ac.ox.cs.pdq.db.sql;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;

// TODO: Auto-generated Javadoc
/**
 * Builds queries for detecting homomorphisms in Derby.
 *
 * @author Efthymia Tsamoura
 * @author Julien leblay
 */
public class DerbyStatementBuilder extends SQLStatementBuilder {

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.homomorphism.AbstractHomomorphismStatementBuilder#setupStatements(java.lang.String)
	 */
	@Override
	public Collection<String> createDatabaseStatements(String databaseName) {
		return new LinkedList<>();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.homomorphism.AbstractHomomorphismStatementBuilder#cleanupStatements(java.lang.String)
	 */
	@Override
	public Collection<String> createDropStatements(String databaseName) {
		LinkedList<String> ret = new LinkedList<>();
		ret.add("drop schema "+databaseName+" restrict");
		return ret;
	}

	/**
	 * Clone.
	 *
	 * @return DerbyHomomorphismStatementBuilder
	 */
	@Override
	public DerbyStatementBuilder clone() {
		return new DerbyStatementBuilder();
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.homomorphism.SQLStatementBuilder#indexDropStatement(uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager.DBRelation, java.lang.StringBuilder, java.lang.StringBuilder)
	 */
	@Override
	protected String createDropIndexStatement(Relation relation, StringBuilder indexName, StringBuilder indexColumns) {
		return "DROP INDEX idx_" + relation.getName() + "_" + indexName ;
	}

	@Override
	public String createBulkInsertStatement(Predicate predicate, Collection<Atom> facts) {
		throw new java.lang.UnsupportedOperationException("No bulk inserts are allowed in Derby");
	}

	@Override
	public String createBulkDeleteStatement(Predicate predicate, Collection<Atom> facts, Map<String, Relation> relationNamesToDatabaseTables) {
		return super.createBulkDeleteStatement(predicate, facts, relationNamesToDatabaseTables);
	}
	
}