package uk.ac.ox.cs.pdq.planner.dag;

import java.util.Collection;

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

}
