package uk.ac.ox.cs.pdq.planner.dag;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClass;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;


/**
 * Dag configuration
 * @author Efthymia Tsamoura
 *
 */
public interface DAGConfiguration extends Configuration<DAGPlan> {
	
	/**
	 * 
	 * @return the high of this configuration
	 */
	Integer getHeight();
	/**
	 * 
	 * @return the id of this configuration
	 */
	Integer getId();
	/**
	 * 
	 * @return the bushiness of this configuration
	 */
	Integer getBushiness();
	/**
	 * 
	 * @return the equivalence class of this configuration
	 */
	DAGEquivalenceClass getEquivalenceClass();
	/**
	 * 
	 * @param eclass
	 * 		Sets the equivalence class of this configuration
	 */
	void setEquivalenceClass(DAGEquivalenceClass eclass);
	/**
	 * 
	 * @return the ranking of this configuration. A ranking is used when the configurations are prioritised. 
	 */
	Double getRanking();
	/**
	 * 
	 * @param ranking
	 * 		Sets the ranking of this configuration.
	 */
	void setRanking(Double ranking);
	/**
	 * 
	 * @return true if the input configuration is a left-deep one
	 */
	Boolean isLeftDeep();

	boolean strictlyIncludes(DAGConfiguration configuration);

	boolean strictlyIncludes(DAGConfiguration left, DAGConfiguration right);
	/**
	 * 
	 * @return the sql statement corresponding to the ApplyRule facts of this configuration
	 */
	String getSql();
	/**
	 * 
	 * @return the object used to cost plans
	 */
	CostEstimator<DAGPlan> getCostEstimator();
	/**
	 * 
	 * @return the subconfigurations of the input configuration
	 */
	Collection<DAGConfiguration> getSubconfigurations();
	/**
	 * @return the collection of ApplyRule sub-configurations
	 */
	Collection<ApplyRule> getApplyRules();
	/**
	 * @return the ApplyRule sub-configurations (ordered by appearance) of the input configuration
	 */
	List<ApplyRule> getApplyRulesList();
	
	@Override
	DAGConfiguration clone();

}
