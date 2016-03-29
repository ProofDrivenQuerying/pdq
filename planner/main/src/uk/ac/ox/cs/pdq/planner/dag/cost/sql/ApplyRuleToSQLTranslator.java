package uk.ac.ox.cs.pdq.planner.dag.cost.sql;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * Translates an ApplyRule configuration to an SQL query.
 *
 * @author Efthymia Tsamoura
 */
public class ApplyRuleToSQLTranslator {

	/** The Constant RELATION_ALIAS_PREFIX. */
	private static final String RELATION_ALIAS_PREFIX = "r";
	
	/** The Constant COLUMN_ALIAS_PREFIX. */
	private static final String COLUMN_ALIAS_PREFIX = "a";

	/**  The configuration to translate. */
	private final ApplyRule configuration;

	/** Constants that should be projected out. These are chase constants that appear in multiple ApplyRules of the same configuration*/
	private final Collection<Constant> toProject;

	/**  The SQL translation. */
	private String sql;

	/**  Aliases for the projected constants. */
	private Map<Constant,String> toProjectToAlias;
	
	/** The to project to expression. */
	private Map<Constant,String> toProjectToExpression;

	/**
	 * Instantiates a new apply rule to sql translator.
	 *
	 * @param configuration 		The configuration to translate
	 * @param toProject 		 Constants that should be projected out. These are chase constants that appear in multiple ApplyRules of the same configuration
	 */
	public ApplyRuleToSQLTranslator(ApplyRule configuration, Collection<Constant> toProject) {
		Preconditions.checkNotNull(configuration);
		this.configuration = configuration;
		this.toProject = toProject;
		this.translate();
	}

	/**
	 * Gets the sql.
	 *
	 * @return String
	 */
	public String getSql() {
		return this.sql;
	}

	/**
	 * Gets the to project to alias.
	 *
	 * @return Map<Constant,String>
	 */
	public Map<Constant,String> getToProjectToAlias() {
		return this.toProjectToAlias;
	}

	/**
	 * Translate.
	 */
	private void translate() {
		Map<Atom,String> factToAlias = this.makeAliases(this.configuration);
		//Find possible join predicates among different facts of the ApplyRule
		Set<String> joinConditions = this.makeJoinConditions(this.configuration, factToAlias);
		//Find possible filtering predicates
		Set<String> filteringConditions = this.makeFilteringConditions(this.configuration, factToAlias);
		Pair<Map<Constant,String>, Map<Constant,String>> mappings = this.makeSelectConditions(this.configuration, this.toProject, factToAlias);
		this.toProjectToExpression = mappings.getLeft();
		this.toProjectToAlias = mappings.getRight();

		String selectStatement = this.makeSelectStatement(this.toProjectToExpression);
		String fromStatement = this.makeFromStatement(factToAlias);
		String whereStatement = this.makeWhereStatement(joinConditions, filteringConditions);

		//Combine the join and the filtering predicates into a single SQL query
		this.sql = selectStatement + "\n" + fromStatement + "\n" + whereStatement;
	}

	/**
	 * Make aliases.
	 *
	 * @param configuration the configuration
	 * @return an alias for each input ApplyRule fact
	 */
	private Map<Atom,String> makeAliases(ApplyRule configuration) {
		int i = 0;
		Map<Atom,String> factToAlias = new HashMap<>();
		for(Atom fact:this.configuration.getFacts()) {
			factToAlias.put(fact, RELATION_ALIAS_PREFIX + i++);
		}
		return factToAlias;
	}

	/**
	 * Make join conditions.
	 *
	 * @param configuration the configuration
	 * @param factToAlias the fact to alias
	 * @return join and selection predicates based on the facts of the ApplyRule configuration
	 */
	private Set<String> makeJoinConditions(ApplyRule configuration, Map<Atom,String> factToAlias) {
		Set<String> joinConditions = new HashSet<>();
		joinConditions.addAll(this.makeInterFactJoinConditions(configuration, factToAlias));
		joinConditions.addAll(this.makeIntraFactJoinConditions(configuration, factToAlias));
		return joinConditions;
	}

	/**
	 * Make inter fact join conditions.
	 *
	 * @param configuration the configuration
	 * @param factToAlias the fact to alias
	 * @return join predicates among different facts of the ApplyRule configuration
	 */
	private Set<String> makeInterFactJoinConditions(ApplyRule configuration, Map<Atom,String> factToAlias) {
		Set<String> joinConditions = new HashSet<>();
		List<Atom> facts = Lists.newArrayList(configuration.getFacts());
		for(int i = 0; i < facts.size() - 1; ++i) {
			Atom fi = facts.get(i);
			for(int j = i + 1; j < facts.size(); ++j) {
				Atom fj = facts.get(j);
				Collection<Term> constants = CollectionUtils.intersection(fi.getTerms(), fj.getTerms());
				for(Term constant:constants) {
					List<Integer> pi = fi.getTermPositions(constant);
					List<Integer> pj = fj.getTermPositions(constant);
					if(pi.size() > 0 && pj.size() > 0) {
						Attribute ai = ((Relation)fi.getSignature()).getAttribute(pi.get(0));
						Attribute aj = ((Relation)fj.getSignature()).getAttribute(pj.get(0));
						joinConditions.add(factToAlias.get(fi) + "." + ai.toString() + "=" + factToAlias.get(fj) + "." + aj.toString());
					}
				}
			}
		}
		return joinConditions;
	}

	/**
	 * Make intra fact join conditions.
	 *
	 * @param configuration the configuration
	 * @param factToAlias the fact to alias
	 * @return selection predicates within single facts of the ApplyRule configuration
	 */
	private Set<String> makeIntraFactJoinConditions(ApplyRule configuration, Map<Atom,String> factToAlias) {
		Set<String> joinConditions = new HashSet<>();
		List<Atom> facts = Lists.newArrayList(configuration.getFacts());
		for(int i = 0; i < facts.size(); ++i) {
			Atom fi = facts.get(i);
			for(Term constant:fi.getTerms()) {
				List<Integer> joinPositions = fi.getTermPositions(constant);
				if(joinPositions.size() > 1) {
					for(int pi = 0; pi < joinPositions.size() - 1; ++pi) {
						Attribute ai = ((Relation)fi.getSignature()).getAttribute(joinPositions.get(pi));
						Attribute aj = ((Relation)fi.getSignature()).getAttribute(joinPositions.get(pi+1));
						joinConditions.add(factToAlias.get(fi) + "." + ai.toString() + "=" + factToAlias.get(fi) + "." + aj.toString());
					}
				}
			}
		}
		return joinConditions;
	}

	/**
	 * Make filtering conditions.
	 *
	 * @param configuration the configuration
	 * @param factToAlias the fact to alias
	 * @return filtering predicates based on the ApplyRule facts
	 */
	private Set<String> makeFilteringConditions(ApplyRule configuration, Map<Atom,String> factToAlias) {
		Set<String> filteringConditions = new HashSet<>();
		List<Atom> facts = Lists.newArrayList(configuration.getFacts());
		for(Atom fact:facts) {
			int i = 0;
			for(Term term:fact.getTerms()) {
				if(!term.isSkolem() && !term.isVariable()) {
					String constant = Utility.format(((TypedConstant)term));
					Attribute ai = ((Relation)fact.getSignature()).getAttribute(i);
					filteringConditions.add(factToAlias.get(fact) + "." + ai.toString() + "=" + constant);
				}
				i++;
			}
		}
		return filteringConditions;
	}


	/**
	 * Make select conditions.
	 *
	 * @param configuration the configuration
	 * @param toProject the to project
	 * @param factToAlias the fact to alias
	 * @return a SQL clause for each constant that will be projected out
	 */
	private Pair<Map<Constant,String>, Map<Constant,String>> makeSelectConditions(ApplyRule configuration, Collection<Constant> toProject, Map<Atom,String> factToAlias) {
		Map<Constant,String> toProjectToAlias =  new HashMap<>();
		Map<Constant,String> toProjectToExpression  =  new HashMap<>();

		Set<Constant> constants = new HashSet<>();
		if(toProject == null || toProject.isEmpty()) {
			for(Atom fact:configuration.getFacts()) {
				constants.addAll(fact.getConstants());
			}
		}
		else {
			constants.addAll(toProject);
		}

		int i = 0;
		for(Constant constant:constants) {
			for(Atom fact:configuration.getFacts()) {
				List<Integer> p = fact.getTermPositions(constant);
				if(!p.isEmpty()) {
					Attribute a = ((Relation)fact.getSignature()).getAttribute(p.get(0));
					String alias = COLUMN_ALIAS_PREFIX + i++;
					String expression = factToAlias.get(fact) + "." + a.toString() + " AS " + alias;
					toProjectToAlias.put(constant, alias);
					toProjectToExpression.put(constant, expression);
					break;
				}
			}
		}

		return Pair.of(toProjectToExpression, toProjectToAlias);
	}

	/**
	 * Make from statement.
	 *
	 * @param factToAlias the fact to alias
	 * @return a FROM statement
	 */
	private String makeFromStatement(Map<Atom,String> factToAlias) {
		String sql = "FROM ";

		int i = 0;
		int size = factToAlias.entrySet().size();
		for(Entry<Atom, String> entry:factToAlias.entrySet()) {
			sql += ((Relation)entry.getKey().getSignature()).getName() + " AS " + entry.getValue();
			if(i < size-1) {
				sql += ",";
			}
			i++;
		}
		return sql;
	}

	/**
	 * Make where statement.
	 *
	 * @param joinConditions the join conditions
	 * @param filteringConditions the filtering conditions
	 * @return a WHERE statement
	 */
	private String makeWhereStatement(Set<String> joinConditions, Set<String> filteringConditions) {
		String sql = "";
		if(!joinConditions.isEmpty() || !filteringConditions.isEmpty()) {
			sql = "WHERE ";
		}
		sql += Joiner.on(" AND ").join(joinConditions);
		if(!joinConditions.isEmpty() && !filteringConditions.isEmpty()) {
			sql += " AND ";
		}
		sql += Joiner.on(" AND ").join(filteringConditions);
		return sql;
	}

	/**
	 * Make select statement.
	 *
	 * @param toProjectToExpression the to project to expression
	 * @return a SELECT statement
	 */
	private String makeSelectStatement(Map<Constant,String> toProjectToExpression) {
		if(toProjectToExpression.isEmpty()) {
			return "SELECT *";
		}
		String sql = "SELECT ";
		sql += Joiner.on(",").join(toProjectToExpression.values());
		return sql;
	}
}