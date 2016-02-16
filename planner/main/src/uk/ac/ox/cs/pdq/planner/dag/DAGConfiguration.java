package uk.ac.ox.cs.pdq.planner.dag;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;


// TODO: Auto-generated Javadoc
/**
 * Type of configurations met in the DAG world. 
 * DAG configurations are built up compositionally and can be either unary or binary.
 * 
 * @author Efthymia Tsamoura
 *
 */
public interface DAGConfiguration extends Configuration<DAGPlan> {
	
	/**
	 * Gets the height.
	 *
	 * @return the hight of this configuration
	 */
	Integer getHeight();
	
	/**
	 * Gets the id.
	 *
	 * @return the id of this configuration
	 */
	Integer getId();
	
	/**
	 * Gets the bushiness.
	 *
	 * @return the bushiness of this configuration
	 */
	Integer getBushiness();

	/**
	 * Checks if is left deep.
	 *
	 * @return true if the input configuration is a left-deep one
	 */
	Boolean isLeftDeep();
	
	/**
	 * Gets the subconfigurations.
	 *
	 * @return the subconfigurations of the input configuration
	 */
	Collection<DAGConfiguration> getSubconfigurations();
	
	/**
	 * Gets the apply rules.
	 *
	 * @return the collection of ApplyRule sub-configurations
	 */
	Collection<ApplyRule> getApplyRules();
	
	/**
	 * Gets the apply rules list.
	 *
	 * @return the ApplyRule sub-configurations (ordered by appearance) of the input configuration
	 */
	List<ApplyRule> getApplyRulesList();
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.reasoning.Configuration#clone()
	 */
	@Override
	DAGConfiguration clone();

}
