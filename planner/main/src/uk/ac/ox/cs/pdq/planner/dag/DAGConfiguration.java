package uk.ac.ox.cs.pdq.planner.dag;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;


/**
 * Configurations represent a node in the search space for a plan or proof.
 * DAG configurations are built up inductively via a composition operator
 * 
 * @author Efthymia Tsamoura
 *
 */
public interface DAGConfiguration extends Configuration {
	
	/**
	 *
	 * @return the hight of this configuration
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
	 * @return true if the input configuration is a left-deep one
	 */
	Boolean isLeftDeep();
	
	/**
	 *
	 * @return the subconfigurations of the input configuration
	 */
	Collection<DAGConfiguration> getSubconfigurations();
	
	/**
	 *
	 * @return the collection of ApplyRule sub-configurations
	 */
	Collection<ApplyRule> getApplyRules();
	
	/**
	 *
	 * @return the ApplyRule sub-configurations (ordered by appearance) of the input configuration
	 */
	List<ApplyRule> getApplyRulesList();
	
//	/* (non-Javadoc)
//	 * @see uk.ac.ox.cs.pdq.planner.reasoning.Configuration#clone()
//	 */
//	@Override
//	DAGConfiguration clone();

}
