package uk.ac.ox.cs.pdq.planner.reasoning;

/**
 * This package contains classes that define a top level configuration interface.
 * Configurations represent states of the planner.
 * Configurations have a direct correspondence with a query plan.
 * Configurations can be either linear or DAG:
 * In the current implementation, the configurations use the chase as a reasoning system.
 *  The chase configurations are associated with
 *  (i) a collection of facts using initial chase constants called the output
 *  facts OF, which will always implicitly include the initial chase
 *  facts, (ii) a subset of the initial chase constants, called the input chase constants IC. 
 *  about which values are accessible. We can derive from the output
 *  facts the collection of output chase constants OC of the configuration: those that are mentioned in the facts OF. 
 *  A configuration with input constants IC and output facts OF represents a proof of OF using the rules of AcSch, starting from the hypothesis that each
	c \in IC is accessible. The (output) facts are all stored inside the state member field.
 * 
 * 
 @author Efthymia Tsamoura
 */
	
