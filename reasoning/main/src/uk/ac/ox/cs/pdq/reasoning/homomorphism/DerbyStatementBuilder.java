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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Builds queries for detecting homomorphisms in Derby
 *
 * @author Efthymia Tsamoura
 * @author Julien leblay
 */
public class DerbyStatementBuilder extends SQLStatementBuilder {

	private static BiMap<String, String> nameEncodings = HashBiMap.create();
	static {
		nameEncodings.put(":", "cl_");
		nameEncodings.put("$", "dl_");
		nameEncodings.put("#", "hs_");
		nameEncodings.put("%", "pc_");
		nameEncodings.put("-", "hp_");
		nameEncodings.put("{", "lbc_");
		nameEncodings.put("}", "rbc_");
		nameEncodings.put("[", "lbk_");
		nameEncodings.put("]", "rbk_");
		nameEncodings.put("(", "lpr_");
		nameEncodings.put(")", "rpr_");
		nameEncodings.put("int", "int_");
		nameEncodings.put("float", "flt_");
		nameEncodings.put("string", "str_");
		nameEncodings.put("boolean", "bool_");
		nameEncodings.put("decimal", "dec_");
		nameEncodings.put("datetime", "date_");
		nameEncodings.put("both", "both_");
		nameEncodings.put("none", "none_");
		nameEncodings.put("check", "chk_");
	}

	/**
	 * Default constructor
	 */
	public DerbyStatementBuilder() {
		super();
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
	protected Collection<String> makeInserts(Collection<? extends Predicate> facts, Map<String, DBRelation> aliases) {
		Collection<String> result = new LinkedList<>();
		for (Predicate fact : facts) {
			Relation alias = aliases.get(fact.getName());
			List<Term> terms = fact.getTerms();
			String insertInto = "INSERT INTO " + this.encodeName(alias.getName()) + " " + "VALUES ( ";
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
		return new DerbyStatementBuilder();
	}

	@Override
	public String encodeName(String name) {
		String result = name;
		for (Map.Entry<String, String> entry: nameEncodings.entrySet()) {
			result = result.replace(entry.getKey(), entry.getValue());
		}
		return result;
	}
}