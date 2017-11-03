package uk.ac.ox.cs.pdq.data.sql;

import java.util.Collection;
import java.util.LinkedList;

import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.sql.DerbyStatementBuilder;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;

public class DerbyDatabaseInstance extends SqlDatabaseInstance {
	
	public DerbyDatabaseInstance(DatabaseParameters parameters) {
		super(parameters);
	}
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
		
		// Derby won't drop a schema if it has contents, so we need a drop statement that finds all tables and drop them all before droping the schema.
		ret.add(
		"SELECT 'ALTER TABLE '||S.SCHEMANAME||'.'||T.TABLENAME||' DROP CONSTRAINT '||C.CONSTRAINTNAME||';' " +
		"FROM SYS.SYSCONSTRAINTS C, SYS.SYSSCHEMAS S, SYS.SYSTABLES T "+
		"WHERE C.SCHEMAID = S.SCHEMAID AND C.TABLEID = T.TABLEID AND " + 
		"S.SCHEMANAME = '" + databaseName + "' " +
		"UNION SELECT 'DROP TABLE ' || schemaname ||'.' || tablename || ';' " +
		"FROM SYS.SYSTABLES INNER JOIN SYS.SYSSCHEMAS ON SYS.SYSTABLES.SCHEMAID = SYS.SYSSCHEMAS.SCHEMAID " +
		"WHERE schemaname='" + databaseName + "'"
		);
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

}
