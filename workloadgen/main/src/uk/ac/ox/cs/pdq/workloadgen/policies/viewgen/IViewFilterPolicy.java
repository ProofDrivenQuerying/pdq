/**
 * 
 */
package uk.ac.ox.cs.pdq.workloadgen.policies.viewgen;

import uk.ac.ox.cs.pdq.workloadgen.policies.IPolicy;
import uk.ac.ox.cs.pdq.workloadgen.query.Query;
import uk.ac.ox.cs.pdq.workloadgen.query.View;

/**
 * Interface for a policy that generates filter predicates for a query
 * 
 * @author herodotos.herodotou
 *
 */
public interface IViewFilterPolicy extends IPolicy {

   /**
    * Create a set of filter predicates for the query.
    * 
    * @param query
    * @return true if the policy added filter predicates that satisfy all of its
    *         parameters
    */
   public boolean createFilterPredicates(Query query, View view);

}
