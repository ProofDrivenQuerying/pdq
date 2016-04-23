/**
 * 
 */
package cy.ac.cut.cs.workloadgen.policies.querygen;

import cy.ac.cut.cs.workloadgen.policies.IPolicy;
import cy.ac.cut.cs.workloadgen.query.Query;

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
