/**
 * 
 */
package cy.ac.cut.cs.workloadgen.policies.viewgen;

import cy.ac.cut.cs.workloadgen.policies.IPolicy;
import cy.ac.cut.cs.workloadgen.query.Query;
import cy.ac.cut.cs.workloadgen.query.View;

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
