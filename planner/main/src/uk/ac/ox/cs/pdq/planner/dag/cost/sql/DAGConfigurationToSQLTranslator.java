package uk.ac.ox.cs.pdq.planner.dag.cost.sql;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.DAGConfiguration;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * Translates a left-deep configuration, where all ApplyRule configurations have input-free bindings, into an SQL query.
 *
 * @author Efthymia Tsamoura
 */
public class DAGConfigurationToSQLTranslator {

	/** The Constant APPLYRULE_ALIAS_PREFIX. */
	private static final String APPLYRULE_ALIAS_PREFIX = "c";

	/**  The configuration to translate. */
	private final DAGConfiguration configuration;

	/**  The SQL translation. */
	private String sql;

	/**
	 * Constructor for DAGConfigurationToSQLTranslator.
	 * @param configuration DAGConfiguration<S>
	 */
	public DAGConfigurationToSQLTranslator(DAGConfiguration configuration) {
		Preconditions.checkNotNull(configuration);
		Preconditions.checkArgument(configuration.isLeftDeep());
		this.configuration = configuration;
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
	 * Translate.
	 */
	private void translate() {

		Map<ApplyRule, String> applyRuleToAlias = this.makeAliases(this.configuration);
		Map<ApplyRule, ApplyRuleToSQLTranslator> applyRuleToTranslator = new HashMap<>();
		Collection<Constant> joinConstants = this.findJoinConstants(this.configuration);
		//Create the SQL queries for each constituting ApplyRule configuration
		for(ApplyRule applyRule:this.configuration.getApplyRules()) {
			ApplyRuleToSQLTranslator translator = new ApplyRuleToSQLTranslator(applyRule, joinConstants);
			applyRuleToTranslator.put(applyRule, translator);
		}
		//Find possible join predicates among different ApplyRules
		Set<String> joinConditions = this.makeJoinConditions(this.configuration, joinConstants, applyRuleToAlias, applyRuleToTranslator);
		String selectStatement = this.makeSelectStatement();
		String fromStatement = this.makeFromStatement(this.configuration.getApplyRulesList(), applyRuleToAlias, applyRuleToTranslator);
		String whereStatement = this.makeWhereStatement(joinConditions);
		//Combine the SQL queries and the join predicates into a single SQL query
		this.sql = selectStatement + "\n" + fromStatement + "\n" + whereStatement;
	}

	/**
	 * Make aliases.
	 *
	 * @param configuration the configuration
	 * @return an alias for each ApplyRule configuration
	 */
	private Map<ApplyRule,String> makeAliases(DAGConfiguration configuration) {
		int i = 0;
		Map<ApplyRule,String> applyRuleToAlias = new HashMap<>();
		for(ApplyRule applyRule:this.configuration.getApplyRules()) {
			applyRuleToAlias.put(applyRule, APPLYRULE_ALIAS_PREFIX + i++);
		}
		return applyRuleToAlias;
	}

	/**
	 * Find join constants.
	 *
	 * @param configuration the configuration
	 * @return the constants that appear in multiple ApplyRule configurations
	 */
	private Collection<Constant> findJoinConstants(DAGConfiguration configuration) {
		Collection<Constant> joinConstants = new HashSet<>();
		List<ApplyRule> applyRules = configuration.getApplyRulesList();
		for(int i = 0; i < applyRules.size() - 1; ++i) {
			for(int j = i + 1; j < applyRules.size(); ++j) {
				joinConstants.addAll(CollectionUtils.intersection(applyRules.get(i).getOutput(), applyRules.get(j).getOutput()));
			}
		}
		return joinConstants;
	}

	/**
	 * Make join conditions.
	 *
	 * @param configuration 		Configuration to translate
	 * @param joinConstants 		Constants that appear in multiple ApplyRule configurations
	 * @param applyRuleToAlias 		Aliases for the ApplyRule queries
	 * @param applyRuleToTranslator 		Translates an ApplyRule into an SQL query
	 * @return join predicates among different ApplyRules
	 */
	private Set<String> makeJoinConditions(DAGConfiguration configuration, Collection<Constant> joinConstants,
			Map<ApplyRule, String> applyRuleToAlias, Map<ApplyRule, ApplyRuleToSQLTranslator> applyRuleToTranslator) {
		Set<String> joinConditions = new HashSet<>();
		List<ApplyRule> applyRules = configuration.getApplyRulesList();
		for(Constant constant:joinConstants) {
			for(int i = 0; i < applyRules.size() - 1; ++i) {
				ApplyRule ai = applyRules.get(i);
				if(ai.getProperOutput().contains(constant)) {
					for(int j = i + 1; j < applyRules.size(); ++j) {
						ApplyRule aj = applyRules.get(j);
						if(aj.getProperOutput().contains(constant)) {
							String expression = applyRuleToAlias.get(ai) + "." + applyRuleToTranslator.get(ai).getToProjectToAlias().get(constant) + "=" +
									applyRuleToAlias.get(aj) + "." + applyRuleToTranslator.get(aj).getToProjectToAlias().get(constant);
							joinConditions.add(expression);
						}
					}
				}
			}
		}
		return joinConditions;
	}

	/**
	 * Make from statement.
	 *
	 * @param applyRules the apply rules
	 * @param applyRuleToAlias the apply rule to alias
	 * @param applyRuleToTranslator the apply rule to translator
	 * @return a FROM statement
	 */
	private String makeFromStatement(List<ApplyRule> applyRules, Map<ApplyRule, String> applyRuleToAlias, Map<ApplyRule, ApplyRuleToSQLTranslator> applyRuleToTranslator) {
		String sql = "FROM ";

		int i = 0;
		int size = applyRuleToAlias.entrySet().size();
		for(ApplyRule ai:applyRules) {
			sql += "(" + applyRuleToTranslator.get(ai).getSql() + ")" + " AS " + applyRuleToAlias.get(ai);
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
	 * @return a WHERE statement
	 */
	private String makeWhereStatement(Set<String> joinConditions) {
		String sql = "";
		if(!joinConditions.isEmpty()) {
			sql = "WHERE ";
			sql += Joiner.on(" AND ").join(joinConditions);
		}
		return sql;
	}

	/**
	 * Make select statement.
	 *
	 * @return a SELECT statement
	 */
	private String makeSelectStatement() {
		String sql = "SELECT *";
		return sql;
	}
}