package uk.ac.ox.cs.pdq.planner.dag.equivalence;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.Dominance;


/**
 * A collection of equivalence classes
 *
 * @author Efthymia Tsamoura
 *
 * @param 
 */
public interface DAGEquivalenceClasses {

	/**
	 * Adds the configuration to its structurally equivalent class
	 * @param configuration
	 */
	void addEntry(DAGChaseConfiguration configuration);

	/**
	 * Removes the input configuration from its class
	 * @param configuration
	 */
	void removeEntry(DAGChaseConfiguration configuration);

	/**
	 * Retains the configurations
	 * @param configurations
	 */
	void retainAll(Collection<DAGChaseConfiguration> configurations);

	/**
	 * @return the configurations of all classes
	 */
	Collection<DAGChaseConfiguration> getConfigurations();

	/**
	 *
	 * @return all classes
	 */
	Collection<DAGEquivalenceClass> getEquivalenceClasses();

	/**
	 * @param configuration
	 * @return the equivalence class of the input configuration
	 */
	DAGEquivalenceClass getEquivalenceClass(DAGChaseConfiguration configuration);

	/**
	 * Removes all input configurations
	 * @param configurations
	 */
	void removeAll(Collection<DAGChaseConfiguration> configurations);

	/**
	 * @param configuration
	 * @return the configurations that are dominated by the input configuration
	 */
	Collection<DAGChaseConfiguration> dominatedBy(Dominance[] dominance, DAGChaseConfiguration configuration);

	/**
	 * @param configuration
	 * @return a configuration that is equivalent to the input configuration
	 */
	DAGChaseConfiguration structurallyEquivalentTo(DAGChaseConfiguration configuration);

	/**
	 * @param configuration
	 * @return a configuration that dominates the input configuration
	 */
	DAGChaseConfiguration dominate(Dominance[] dominance, DAGChaseConfiguration configuration);

	/**
	 * @param configuration
	 * @return true if at least one class contains the input configuration
	 */
	boolean contains(DAGChaseConfiguration configuration);

	/**
	 * @return an iterator over the classes
	 */
	Iterator<Entry<DAGChaseConfiguration, SynchronizedEquivalenceClass>> iterator();

	/**
	 * @return true if all classes are empty
	 */
	boolean isEmpty();

	void clear();

	/**
	 * @return int
	 */
	int size();

	/**
	 * @return double
	 */
	double averageClassSize();

	/**
	 * @return int
	 */
	int medianClassSize();
}