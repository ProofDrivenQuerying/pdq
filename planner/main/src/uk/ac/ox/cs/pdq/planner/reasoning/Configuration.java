package uk.ac.ox.cs.pdq.planner.reasoning;

import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.util.Costable;

// TODO: Auto-generated Javadoc
/**
 * Configurations represent derivation of implicit information using constraints
 * and have a direct correspondence with a query plan. 
 * This interface is generic as it does not bind a reasoning mechanism to the configuration.  
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 * @param <P> the generic type
 */
public interface Configuration<P extends Plan> extends Costable, Cloneable, Comparable<Configuration<P>>{
	
	/**
	 * Gets the plan.
	 *
	 * @return 		the plan of this configuration
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
	 * @return true if the configuration matches the input query.
	 * (Conjunctive query match definition) If Q′ is a conjunctive query and v is a chase configuration
	 * having elements for each free variable of Q′, then a homomorphism of Q′ into v
	 * mapping each free variable into the corresponding element is called a match for Q′ in v.
	 */
	boolean isSuccessful(ConjunctiveQuery query);

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	int compareTo(Configuration<P> o);
}
