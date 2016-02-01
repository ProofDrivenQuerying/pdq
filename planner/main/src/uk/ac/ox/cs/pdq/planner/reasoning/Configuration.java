package uk.ac.ox.cs.pdq.planner.reasoning;

import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.util.Costable;

/**
 * Configurations represent derivation of implicit information using constraints
 * and have a direct correspondence with a query plan. 
 * This interface is generic as it does not bind a reasoning mechanism to the configuration.  
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public interface Configuration<P extends Plan> extends Costable, Cloneable, Comparable<Configuration<P>>{
	
	/**
	 * @return 
	 * 		the plan of this configuration
	 */
	P getPlan();
	
	void setPlan(P plan);
	
	Configuration<P> clone();
	
	/**
	 * 
	 * @param query
	 * @return
	 * true if the configuration matches the input query.
	 * (Conjunctive query match definition) If Q′ is a conjunctive query and v is a chase configuration
	 * having elements for each free variable of Q′, then a homomorphism of Q′ into v
	 * mapping each free variable into the corresponding element is called a match for Q′ in v. 
	 */
	boolean isSuccessful(Query<?> query);

	int compareTo(Configuration<P> o);
}
