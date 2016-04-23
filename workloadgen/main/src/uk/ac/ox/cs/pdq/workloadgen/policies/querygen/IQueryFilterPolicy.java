/**
 * 
 */
package uk.ac.ox.cs.pdq.workloadgen.policies.querygen;

import uk.ac.ox.cs.pdq.workloadgen.policies.IPolicy;
import uk.ac.ox.cs.pdq.workloadgen.query.Query;

/**
 * Interface for a policy that generates filter predicates for a query
 * 
 * @author herodotos.herodotou
 *
 */
public interface IQueryFilterPolicy extends IPolicy {

   /**
    * Create a set of filter predicates for the query.
    * 
    * @param query
    * @return true if the policy added filter predicates that satisfy all of its
    *         parameters
    */
   public boolean createFilterPredicates(Query query);

}
