
package uk.ac.ox.cs.pdq.reasoning.homomorphism;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager.DBRelation;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint.TopKConstraint;

import com.google.common.base.Joiner;
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
	 * @param relation the table to create
	 * @return a SQL statement that creates the fact table of the given relation
	 */
	protected String createTableStatement(Relation relation) {
		StringBuilder result = new StringBuilder();
		result.append("CREATE TABLE  ").append(this.encodeName(relation.getName())).append('(');
		for (int it = 0; it < relation.getAttributes().size(); ++it) {
			result.append(' ').append(relation.getAttributes().get(it).getName());
			if (relation.getAttribute(it).getType() instanceof Class && String.class.isAssignableFrom((Class) relation.getAttribute(it).getType())) {
				result.append(" VARCHAR(500),");
			}
			else if (relation.getAttribute(it).getType() instanceof Class && Integer.class.isAssignableFrom((Class) relation.getAttribute(it).getType())) {
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
			
			//Create the VALUES field of the source data table
			String values = "VALUES(";
			for (Term term : terms) {
				if (!term.isVariable()) {
					values += "'" + term + "'" + ",";
				}
			}
			values += 0 + ",";
			values += fact.getId();
			values += ")";
		
			//Create the table header of the source data table 
			String sourceHeader = "(" + Joiner.on(",").join(rel.getAttributes()) + ")";
			
			String insertedValues = "";
			for(int i = 0; i < rel.getAttributes().size(); ++i) {
				insertedValues += "source." + rel.getAttributes().get(i);
				if(i < rel.getAttributes().size()-1) {
					insertedValues += ",";
				}
			}
			insertedValues = "(" + insertedValues + ")";
			
			//Create the MERGE statement
			String mergeStatement = "MERGE INTO " + "\n" +
					this.encodeName(rel.getName()) + "\n" + 
					"USING (" + values + ")" + " AS source" + sourceHeader + "\n" + 
					"ON " + this.encodeName(rel.getName()) + "." + rel.getAttribute(rel.getAttributes().size()-1) + "=" + "source" + "." + rel.getAttribute(rel.getAttributes().size()-1) + "\n" +
					"WHEN NOT MATCHED THEN " + "\n" +
					"INSERT " + sourceHeader + "\n" +
					"VALUES " + insertedValues;
			result.add(mergeStatement);
		}
		return result;
		
		
		/**
		 * MERGE 
			   member_topic
			USING ( 
			    VALUES (0, 110, 'test')
			) AS foo (mt_member, mt_topic, mt_notes) 
			ON member_topic.mt_member = foo.mt_member 
			   AND member_topic.mt_topic = foo.mt_topic
			WHEN MATCHED THEN
			   UPDATE SET mt_notes = foo.mt_notes
			WHEN NOT MATCHED THEN
			   INSERT (mt_member, mt_topic, mt_notes)
			   VALUES (mt_member, mt_topic, mt_notes)
			;
		 */
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