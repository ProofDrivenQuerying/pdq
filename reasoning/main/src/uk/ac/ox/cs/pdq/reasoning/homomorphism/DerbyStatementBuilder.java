package uk.ac.ox.cs.pdq.reasoning.homomorphism;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager.DBRelation;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint.TopKConstraint;

import com.google.common.collect.BiMap;

/**
 * Builds queries for detecting homomorphisms in Derby
 *
 * @author Efthymia Tsamoura
 * @author Julien leblay
 */
public class DerbyStatementBuilder extends SQLStatementBuilder {

	/**
	 * Default constructor
	 */
	public DerbyStatementBuilder() {
		super();
	}
	
	protected DerbyStatementBuilder(BiMap<String, String> cleanMap) {
		super(cleanMap);
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.homomorphism.AbstractHomomorphismStatementBuilder#setupStatements(java.lang.String)
	 */
	@Override
	public Collection<String> setupStatements(String databaseName) {
		return new LinkedList<>();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.homomorphism.AbstractHomomorphismStatementBuilder#cleanupStatements(java.lang.String)
	 */
	@Override
	public Collection<String> cleanupStatements(String databaseName) {
		return new LinkedList<>();
	}
	
	
	@Override
	protected String translateLimitConstraints(Evaluatable source, HomomorphismConstraint... constraints) {
		for(HomomorphismConstraint c:constraints) {
			if(c instanceof TopKConstraint) {
				return "FETCH NEXT " + ((TopKConstraint) c).k + " ROWS ONLY  ";
			}
		}
		return null;
	}

	/**
	 * @return DerbyHomomorphismStatementBuilder
	 */
	@Override
	public DerbyStatementBuilder clone() {
		return new DerbyStatementBuilder(this.cleanMap);
	}

	@Override
	public String encodeName(String name) {
		return super.encodeName(name);
	}
	
	
	/**
	 * 
	 */
	@Override
	protected Pair<String,String> createTableIndex(boolean isForQuery, DBRelation relation, Integer... columns) {
		StringBuilder indexName = new StringBuilder();
		StringBuilder indexColumns = new StringBuilder();
		String sep1 = "", sep2 = "";
		for (Integer i: columns) {
			indexName.append(sep1).append(relation.getAttribute(i).getName());
			indexColumns.append(sep2).append(relation.getAttribute(i).getName());
			sep1 = "_";
			sep2 = ",";
		}
		if(isForQuery)
			indexName.append("Q");
		
		String create ="CREATE INDEX idx_" + this.encodeName(relation.getName()) + "_" + indexName +
				" ON " + this.encodeName(relation.getName()) + "(" + indexColumns + ")";
		String drop ="DROP INDEX idx_" + this.encodeName(relation.getName()) + "_" + indexName;
		return new ImmutablePair<String, String>(create,drop);
	}	
}