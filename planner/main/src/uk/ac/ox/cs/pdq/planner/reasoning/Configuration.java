package uk.ac.ox.cs.pdq.planner.reasoning;

import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.util.Costable;

/**
 * A sequence of chase states and the associated subplan
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public interface Configuration<P extends Plan> extends Costable, Cloneable, Comparable<Configuration<P>>{
	
	/**
	 * @return the plan of this configuration
	 */
	P getPlan();
	
	void setPlan(P plan);
	
	Configuration<P> clone();
	
	/**
	 * @return true if the configuration matches the target query 
	 */
	boolean isSuccessful(Query<?> query);

	int compareTo(Configuration<P> o);
}
