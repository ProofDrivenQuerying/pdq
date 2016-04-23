/**
 * 
 */
package cy.ac.cut.cs.workloadgen.policies.viewgen;

import java.util.List;

import cy.ac.cut.cs.workloadgen.policies.Parameter;
import cy.ac.cut.cs.workloadgen.policies.Parameter.InvalidParameterException;
import cy.ac.cut.cs.workloadgen.query.Query;
import cy.ac.cut.cs.workloadgen.query.View;
import cy.ac.cut.cs.workloadgen.schema.Schema;
import cy.ac.cut.cs.workloadgen.schema.Table;

/**
 * Implements a join policy for query generation that creates a fixed number of
 * foreign-key joins and a fixed number non-foreign-key joins
 * 
 * @author herodotos.herodotou
 */
public class VJoinPolicyFixed extends VJoinPolicy {

   private int numJoins = 0;
   private int numFKJoins = 0;

   /**
    * Default constructor
    */
   public VJoinPolicyFixed() {
      super();
   }

   @Override
   public boolean initialize(List<Parameter> params)
         throws InvalidParameterException {

      // Get the two parameters
      for (Parameter param : params) {
         try {
            if (param.getName().equalsIgnoreCase("NumJoins")) {
               numJoins = Integer.parseInt(param.getValue());
            } else if (param.getName().equalsIgnoreCase("NumFKJoins")) {
               numFKJoins = Integer.parseInt(param.getValue());
            } else {
               throw new InvalidParameterException("Unexpected parameter name="
                     + param.getName());
            }
         } catch (NumberFormatException e) {
            throw new InvalidParameterException("Invalid parameter", e);
         }
      }

      // Validate the parameters
      return numJoins > 0 && numFKJoins > 0 && numFKJoins <= numJoins;
   }

   @Override
   public boolean createJoinConditions(Schema schema, Query query, View view) {

      Table t = selectRandomTable(query);
      if (!view.addTable(t))
         return false;

      int countFKJoins = 0;
      int countJKJoins = 0;
      int count = 0;

      while (count < numJoins) {
         int currIter = count;

         // Try adding a foreign-key join
         if (countFKJoins < numFKJoins) {
            if (addRandomForeignKeyJoin(query, view)) {
               ++countFKJoins;
               ++count;
            }
         }

         // Try adding a non-foreign-key join
         if (countJKJoins < numJoins - numFKJoins) {
            if (addRandomJoinableKeyJoin(query, view)) {
               ++countJKJoins;
               ++count;
            }
         }

         if (currIter == count) {
            // Failed to add either type of join
            return false;
         }
      }

      return true;
   }

}
