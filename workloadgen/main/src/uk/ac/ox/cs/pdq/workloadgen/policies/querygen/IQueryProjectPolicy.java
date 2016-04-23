/**
 * 
 */
package uk.ac.ox.cs.pdq.workloadgen.policies.querygen;

import uk.ac.ox.cs.pdq.workloadgen.policies.IPolicy;
import uk.ac.ox.cs.pdq.workloadgen.query.Query;

/**
 * Interface for a policy that generates project attributes for a query
 * 
 * @author herodotos.herodotou
 *
 */
public interface IQueryProjectPolicy extends IPolicy {

   /**
    * Create a set of attributes to project for the query.
    * 
    * @param query
    * @return true if the policy added attributes that satisfy all of its
    *         parameters
    */
   public boolean createProjectionAttributes(Query query);

}
