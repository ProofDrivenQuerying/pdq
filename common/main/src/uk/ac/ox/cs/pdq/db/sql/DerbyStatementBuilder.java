package uk.ac.ox.cs.pdq.db.sql;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.homomorphism.HomomorphismProperty;
import uk.ac.ox.cs.pdq.db.homomorphism.HomomorphismProperty.TopKProperty;
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
		return new LinkedList<>();
	}


	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.homomorphism.SQLStatementBuilder#translateLimitConstraints(uk.ac.ox.cs.pdq.fol.Evaluatable, uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint[])
	 */
	@Override
	public String translateLimitConstraints(HomomorphismProperty... constraints) {
		for(HomomorphismProperty c:constraints) {
			if(c instanceof TopKProperty) {
				return "FETCH NEXT " + ((TopKProperty) c).k + " ROWS ONLY  ";
			}
		}
		return null;
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
	public String createBulkInsertStatement(Predicate predicate, Collection<Atom> facts, Map<String, Relation> toDatabaseTables) {
		throw new java.lang.UnsupportedOperationException("No bulk inserts are allowed in Derby");
	}

	@Override
	public String createBulkDeleteStatement(Predicate predicate, Collection<Atom> facts, Map<String, Relation> toDatabaseTables) {
		return super.createBulkDeleteStatement(predicate, facts, toDatabaseTables);
	}
}