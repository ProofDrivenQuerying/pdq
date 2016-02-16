/*
 * 
 */
package uk.ac.ox.cs.pdq.planner.dag.equivalence;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

import uk.ac.ox.cs.pdq.planner.dag.DAGAnnotatedPlan;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.Dominance;


// TODO: Auto-generated Javadoc
/**
 * A collection of equivalence classes.
 *
 * @author Efthymia Tsamoura
 */
public interface DAGAnnotatedPlanClasses {

	/**
	 * Adds the configuration to its structurally equivalent class.
	 *
	 * @param configuration the configuration
	 */
	void addEntry(DAGAnnotatedPlan configuration);

	/**
	 * Removes the input configuration from its class.
	 *
	 * @param configuration the configuration
	 */
	void removeEntry(DAGAnnotatedPlan configuration);

	/**
	 * Retains the configurations.
	 *
	 * @param configurations the configurations
	 */
	void retainAll(Collection<DAGAnnotatedPlan> configurations);

	/**
	 * Gets the configurations.
	 *
	 * @return the configurations of all classes
	 */
	Collection<DAGAnnotatedPlan> getConfigurations();

	/**
	 * Gets the equivalence classes.
	 *
	 * @return all classes
	 */
	Collection<DAGAnnotatedPlanClass> getEquivalenceClasses();

	/**
	 * Gets the equivalence class.
	 *
	 * @param configuration the configuration
	 * @return the equivalence class of the input configuration
	 */
	DAGAnnotatedPlanClass getEquivalenceClass(DAGAnnotatedPlan configuration);

	/**
	 * Removes all input configurations.
	 *
	 * @param configurations the configurations
	 */
	void removeAll(Collection<DAGAnnotatedPlan> configurations);

	/**
	 * Dominated by.
	 *
	 * @param dominance the dominance
	 * @param configuration the configuration
	 * @return the configurations that are dominated by the input configuration
	 */
	Collection<DAGAnnotatedPlan> dominatedBy(Dominance[] dominance, DAGAnnotatedPlan configuration);

	/**
	 * Structurally equivalent to.
	 *
	 * @param configuration the configuration
	 * @return a configuration that is equivalent to the input configuration
	 */
	DAGAnnotatedPlan structurallyEquivalentTo(DAGAnnotatedPlan configuration);

	/**
	 * Dominate.
	 *
	 * @param dominance the dominance
	 * @param configuration the configuration
	 * @return a configuration that dominates the input configuration
	 */
	DAGAnnotatedPlan dominate(Dominance[] dominance, DAGAnnotatedPlan configuration);

	/**
	 * Contains.
	 *
	 * @param configuration the configuration
	 * @return true if at least one class contains the input configuration
	 */
	boolean contains(DAGAnnotatedPlan configuration);

	/**
	 * Iterator.
	 *
	 * @return an iterator over the classes
	 */
	Iterator<Entry<DAGAnnotatedPlan, SynchronizedAnnotatedPlanClass>> iterator();

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