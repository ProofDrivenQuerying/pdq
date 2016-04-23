/**
 * 
 */
package cy.ac.cut.cs.workloadgen.policies.viewgen;

import cy.ac.cut.cs.workloadgen.policies.IPolicy;
import cy.ac.cut.cs.workloadgen.query.Query;
import cy.ac.cut.cs.workloadgen.query.View;

/**
 * Interface for a policy that generates project attributes for a query
 * 
 * @author herodotos.herodotou
 *
 */
public interface IViewProjectPolicy extends IPolicy {

   /**
    * Create a set of attributes to project for the query.
    * 
    * @param query
    * @return true if the policy added attributes that satisfy all of its
    *         parameters
    */
   public boolean createProjectionAttributes(Query query, View view);

}
