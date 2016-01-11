package uk.ac.ox.cs.pdq.planner.dag.equivalence;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

import uk.ac.ox.cs.pdq.planner.dag.DAGAnnotatedPlan;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.Dominance;


/**
 * A collection of equivalence classes
 *
 * @author Efthymia Tsamoura
 *
 */
public interface DAGAnnotatedPlanClasses {

	/**
	 * Adds the configuration to its structurally equivalent class
	 * @param configuration
	 */
	void addEntry(DAGAnnotatedPlan configuration);

	/**
	 * Removes the input configuration from its class
	 * @param configuration
	 */
	void removeEntry(DAGAnnotatedPlan configuration);

	/**
	 * Retains the configurations
	 * @param configurations
	 */
	void retainAll(Collection<DAGAnnotatedPlan> configurations);

	/**
	 * @return the configurations of all classes
	 */
	Collection<DAGAnnotatedPlan> getConfigurations();

	/**
	 *
	 * @return all classes
	 */
	Collection<DAGAnnotatedPlanClass> getEquivalenceClasses();

	/**
	 * @param configuration
	 * @return the equivalence class of the input configuration
	 */
	DAGAnnotatedPlanClass getEquivalenceClass(DAGAnnotatedPlan configuration);

	/**
	 * Removes all input configurations
	 * @param configurations
	 */
	void removeAll(Collection<DAGAnnotatedPlan> configurations);

	/**
	 * @param configuration
	 * @return the configurations that are dominated by the input configuration
	 */
	Collection<DAGAnnotatedPlan> dominatedBy(Dominance[] dominance, DAGAnnotatedPlan configuration);

	/**
	 * @param configuration
	 * @return a configuration that is equivalent to the input configuration
	 */
	DAGAnnotatedPlan structurallyEquivalentTo(DAGAnnotatedPlan configuration);

	/**
	 * @param configuration
	 * @return a configuration that dominates the input configuration
	 */
	DAGAnnotatedPlan dominate(Dominance[] dominance, DAGAnnotatedPlan configuration);

	/**
	 * @param configuration
	 * @return true if at least one class contains the input configuration
	 */
	boolean contains(DAGAnnotatedPlan configuration);

	/**
	 * @return an iterator over the classes
	 */
	Iterator<Entry<DAGAnnotatedPlan, SynchronizedAnnotatedPlanClass>> iterator();

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