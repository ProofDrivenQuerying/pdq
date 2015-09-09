package uk.ac.ox.cs.pdq.planner.dag;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import uk.ac.ox.cs.pdq.algebra.Operators;
import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClass;
import uk.ac.ox.cs.pdq.planner.dag.sql.DAGConfigurationToSQLTranslator;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;
import uk.ac.ox.cs.pdq.planner.reasoning.Proof.ProofState;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.Dominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * 
 * @author Efthymia Tsamoura
 *
 * @param <S>
 */
public abstract class DAGChaseConfiguration extends ChaseConfiguration<DAGPlan> implements DAGConfiguration {

	private final Integer id;

	private static Integer globalId = 0;
	
	/** The depth of this configuration */
	private final Integer high;

	/** The bushiness of this configuration */
	private final Integer bushiness;
	
	private final CostEstimator<DAGPlan> costEstimator;
	
	/** True if the configuration is a left-deep one*/
	private Boolean isLeftDeep = null;
	
	/** The ranking of this configuration. Used when configurations are prioritised */
	private Double ranking = Double.MAX_VALUE;

	/** The sql statement corresponding to the ApplyRule facts of this configuration */
	private String sql = null;
	
	/** The configuration's ApplyRule sub-configurations */
	private Collection<ApplyRule> rules = null;

	/** The configuration's ApplyRule sub-configurations ordered according to their appearance */
	private List<ApplyRule> rulesList = null;

	/** The configuration's sub-configurations */
	private Collection<DAGConfiguration> subconfigurations = null;
	
	/** The equivalence class this configuration belongs to*/
	private DAGEquivalenceClass eclass = null;
	

	/**
	 * 
	 * @param accessibleSchema
	 * @param query
	 * 		The input query
	 * @param chaser
	 * 		Chase reasoner
	 * @param state
	 * 		The state of this configuration.
	 * @param proofState
	 * 		The proof state of this configuration
	 * @param flow
	 * 		The control flow of the configuration's plan
	 * @param input
	 * 		The input constants
	 * @param output
	 * 		The output constants
	 * @param dominance
	 * 		Perform dominance checks
	 * @param successDominance
	 * 		Performs success dominance checks
	 * @param high
	 * 		The depth of this configuration
	 * @param bushiness
	 * 		The bushiness of this configuration
	 * @param costEstimator
	 * 		Estimates the configuration's plan
	 */
	public DAGChaseConfiguration(
			AccessibleSchema accessibleSchema, 
			Query<?> query, 
			Chaser chaser,
			AccessibleChaseState state, 
			List<ProofState> proofState, 
			Collection<Constant> input,
			Collection<Constant> output, 
			Dominance[] dominance, 
			SuccessDominance successDominance,
			Integer high,
			Integer bushiness,
			CostEstimator<DAGPlan> costEstimator) {
		super(accessibleSchema, query, chaser, state, proofState, input, output, dominance, successDominance);
		Preconditions.checkNotNull(this.getInput());
		Preconditions.checkNotNull(this.getOutput());
		Preconditions.checkNotNull(costEstimator);
		this.id = globalId++;
		this.high = high;
		this.bushiness = bushiness;
		this.costEstimator = costEstimator;
	}

	public abstract DAGChaseConfiguration clone();

	@Override
	public Integer getHeight() {
		return this.high;
	}

	@Override
	public Integer getBushiness() {
		return this.bushiness;
	}

	@Override
	public Integer getId() {
		return this.id;
	}

	@Override
	public CostEstimator<DAGPlan> getCostEstimator() {
		return this.costEstimator;
	}
		

	@Override
	public String getSql() {
		if(this.sql == null) {
			DAGConfigurationToSQLTranslator translator = new DAGConfigurationToSQLTranslator(this);
			this.sql = translator.getSql();
		}
		return this.sql;
	}
	

	public Double getRanking() {
		return this.ranking;
	}
	

	public void setRanking(Double ranking) {
		this.ranking = ranking;
	}
	
	public Collection<DAGConfiguration> getSubconfigurations() {
		if(this.subconfigurations == null) {
			this.subconfigurations = ConfigurationUtility.getSubconfigurations(this);
		}
		return this.subconfigurations;
	}

	public Collection<ApplyRule> getApplyRules() {
		if(this.rules == null) {
			this.rules = ConfigurationUtility.getApplyRules(this);
		}
		return this.rules;
	}

	public List<ApplyRule> getApplyRulesList() {
		if(this.rulesList == null) {
			this.rulesList = ConfigurationUtility.getApplyRulesList(this);
		}
		return this.rulesList;
	}
	
	@Override
	public Boolean isLeftDeep() {
		if(this.isLeftDeep == null) {
			this.isLeftDeep = ConfigurationUtility.isLeftDeep(this);
		}
		return this.isLeftDeep;
	}

	@Override
	public DAGEquivalenceClass getEquivalenceClass() {
		return this.eclass;
	}

	@Override
	public void setEquivalenceClass(DAGEquivalenceClass eclass) {
		this.eclass = eclass;
	}

	@Override
	public boolean strictlyIncludes(DAGConfiguration configuration) {
		return Collections.indexOfSubList(this.getApplyRulesList(), configuration.getApplyRulesList()) != -1;
	}

	@Override
	public boolean strictlyIncludes(DAGConfiguration left, DAGConfiguration right) {
		List<ApplyRule> l = Lists.newArrayList();
		l.addAll(left.getApplyRulesList());
		l.addAll(right.getApplyRulesList());
		return Collections.indexOfSubList(this.getApplyRulesList(), l) != -1;
	}
	
	
	@Override
	public int compareTo(Configuration o) {
		return this.getPlan().compareTo(o.getPlan());
	}

	@Override
	public void addProjection() {
		RelationalOperator project = Operators.createFinalProjection(
				this.query,
				this.getPlan().getOperator());
		DAGPlan plan = new DAGPlan(project);
		plan.addChild(this.getPlan());
		plan.setCost(this.getPlan().getCost());
		this.setPlan(plan);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return this.getClass().isInstance(o)
				&& this.id.equals(((DAGChaseConfiguration) o).getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}

}
