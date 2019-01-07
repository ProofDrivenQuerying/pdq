package uk.ac.ox.cs.pdq.planner.dag.equivalence;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dominance.Dominance;


/**
 * Collections of DAG configuration equivalence classes.
 * 
 * TOCOMMENT: CHECK IF THE COMMENTS HERE ARE STILL RELEVANT
 *  A mapping h from the chase constants of one configuration
	conf to the chase constants of another configuration conf'
	is fact-preserving if it preserves inferred accessible output facts
	in going from conf to conf' and if the h image of every input
	constant of conf is an input constant of conf'.
	Configurations conf, conf' are fact-equivalent 
	if there is a bijective fact-preserving mapping h between them.
 *
 * @author Efthymia Tsamoura
 *
 */
public interface DAGEquivalenceClasses {

	/**
	 * Adds the configuration to its structurally equivalent class.
	 *
	 * @param configuration the configuration
	 */
	void addEntry(DAGChaseConfiguration configuration);

	/**
	 * Removes the input configuration from its class.
	 *
	 * @param configuration the configuration
	 */
	void removeEntry(DAGChaseConfiguration configuration);

	/**
	 * Retains the configurations.
	 *
	 * @param configurations the configurations
	 */
	void retainAll(Collection<DAGChaseConfiguration> configurations);

	/**
	 * Gets the configurations.
	 *
	 * @return the configurations of all classes
	 */
	Collection<DAGChaseConfiguration> getConfigurations();

	/**
	 * Gets the equivalence classes.
	 *
	 * @return all classes
	 */
	Collection<DAGEquivalenceClass> getEquivalenceClasses();

	/**
	 * Gets the equivalence class.
	 *
	 * @param configuration the configuration
	 * @return the equivalence class of the input configuration
	 */
	DAGEquivalenceClass getEquivalenceClass(DAGChaseConfiguration configuration);

	/**
	 * Removes all input configurations.
	 *
	 * @param configurations the configurations
	 */
	void removeAll(Collection<DAGChaseConfiguration> configurations);

	/**
	 * Dominated by.
	 *
	 * @param dominance the dominance
	 * @param configuration the configuration
	 * @return the configurations that are dominated by the input configuration
	 */
	Collection<DAGChaseConfiguration> dominatedBy(Dominance[] dominance, DAGChaseConfiguration configuration);

	/**
	 * Structurally equivalent to.
	 *
	 * @param configuration the configuration
	 * @return a configuration that is equivalent to the input configuration
	 */
	DAGChaseConfiguration structurallyEquivalentTo(DAGChaseConfiguration configuration);

	/**
	 * Dominate.
	 *
	 * @param dominance the dominance
	 * @param configuration the configuration
	 * @return a configuration that dominates the input configuration
	 */
	DAGChaseConfiguration dominate(Dominance[] dominance, DAGChaseConfiguration configuration);

	/**
	 * Contains.
	 *
	 * @param configuration the configuration
	 * @return true if at least one class contains the input configuration
	 */
	boolean contains(DAGChaseConfiguration configuration);

	/**
	 * Iterator.
	 *
	 * @return an iterator over the classes
	 */
	Iterator<Entry<DAGChaseConfiguration, SynchronizedEquivalenceClass>> iterator();

	/**
	 * Checks if is empty.
	 *
	 * @return true if all classes are empty
	 */
	boolean isEmpty();

	/**
	 * Clear.
	 */
	void clear();

	/**
	 * Size.
	 *
	 * @return int
	 */
	int size();

	/**
	 * Average class size.
	 *
	 * @return double
	 */
	double averageClassSize();

	/**
	 * Median class size.
	 *
	 * @return int
	 */
	int medianClassSize();
}