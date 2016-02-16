/*
 * 
 */
package uk.ac.ox.cs.pdq.planner.reasoning;

import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.util.Costable;

// TODO: Auto-generated Javadoc
/**
 * A sequence of chase states and the associated subplan.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 * @param <P> the generic type
 */
public interface Configuration<P extends Plan> extends Costable, Cloneable, Comparable<Configuration<P>>{
	
	/**
	 * Gets the plan.
	 *
	 * @return the plan of this configuration
	 */
	P getPlan();
	
	/**
	 * Sets the plan.
	 *
	 * @param plan the new plan
	 */
	void setPlan(P plan);
	
	/**
	 * Clone.
	 *
	 * @return the configuration
	 */
	Configuration<P> clone();
	
	/**
	 * Checks if is successful.
	 *
	 * @param query the query
	 * @return true if the configuration matches the target query
	 */
	boolean isSuccessful(Query<?> query);

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	int compareTo(Configuration<P> o);
}
