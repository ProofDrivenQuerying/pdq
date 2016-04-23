/*
 * 
 */
package uk.ac.ox.cs.pdq.cardinality.dag.explorer.filters;

import java.util.Collection;

import uk.ac.ox.cs.pdq.cardinality.dag.DAGAnnotatedPlan;


// TODO: Auto-generated Javadoc
/**
 * Filters out configurations.
 *
 * @author Efthymia Tsamoura
 */
public interface Filter {

	/**
	 * Filter.
	 *
	 * @param configurations the configurations
	 * @return the configurations that are filtered out
	 */
	Collection<DAGAnnotatedPlan> filter(Collection<DAGAnnotatedPlan> configurations);

}
