/*
 * 
 */
package uk.ac.ox.cs.pdq.cardinality.dag;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.cardinality.reasoning.Configuration;
import uk.ac.ox.cs.pdq.plan.DAGPlan;


// TODO: Auto-generated Javadoc
/**
 * Dag configuration.
 *
 * @author Efthymia Tsamoura
 */
public interface DAGConfiguration extends Configuration<DAGPlan> {
	
	/**
	 * Gets the height.
	 *
	 * @return the high of this configuration
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
	 * Gets the unary annotated plans.
	 *
	 * @return the collection of ApplyRule sub-configurations
	 */
	Collection<UnaryAnnotatedPlan> getUnaryAnnotatedPlans();
	
	/**
	 * Gets the unary annotated plans list.
	 *
	 * @return the ApplyRule sub-configurations (ordered by appearance) of the input configuration
	 */
	List<UnaryAnnotatedPlan> getUnaryAnnotatedPlansList();
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.reasoning.Configuration#clone()
	 */
	@Override
	DAGConfiguration clone();

}
