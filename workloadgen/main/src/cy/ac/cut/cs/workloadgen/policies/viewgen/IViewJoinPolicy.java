/**
 * 
 */
package cy.ac.cut.cs.workloadgen.policies.viewgen;

import cy.ac.cut.cs.workloadgen.policies.IPolicy;
import cy.ac.cut.cs.workloadgen.query.Query;
import cy.ac.cut.cs.workloadgen.query.View;
import cy.ac.cut.cs.workloadgen.schema.Schema;

/**
 * Interface for a policy that generates join conditions for a query
 * 
 * @author herodotos.herodotou
 *
 */
public interface IViewJoinPolicy extends IPolicy {

   /**
    * Create a set of join conditions for the query. Add the corresponding
    * tables and join conditions in the provided query.
    * 
    * @param schema
    * @param query
    * @return true if the policy generated a query that satisfies all of its
    *         parameters
    */
   public boolean createJoinConditions(Schema schema, Query query, View view);

}
