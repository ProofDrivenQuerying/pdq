/**
 * 
 */
package uk.ac.ox.cs.pdq.workloadgen.policies.viewgen;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.workloadgen.query.Query;
import uk.ac.ox.cs.pdq.workloadgen.query.View;
import uk.ac.ox.cs.pdq.workloadgen.schema.Schema;

/**
 * High-level query generation policy that uses the join, filter, and project
 * policies for constructing queries.
 * 
 * @author herodotos.herodotou
 */
public class ViewGenPolicy {

   private int numViews;
   private IViewJoinPolicy joinPolicy;
   private IViewFilterPolicy filterPolicy;
   private IViewProjectPolicy projectPolicy;

   private static int MAX_ATTEMPTS = 100;

   /**
    * @param numViews
    */
   public ViewGenPolicy(int numViews) {
      this.numViews = numViews;
      this.joinPolicy = null;
      this.filterPolicy = null;
      this.projectPolicy = null;
   }

   /**
    * @param joinPolicy
    *           the joinPolicy to set
    */
   public void setJoinPolicy(IViewJoinPolicy joinPolicy) {
      this.joinPolicy = joinPolicy;
   }

   /**
    * @param filterPolicy
    *           the filterPolicy to set
    */
   public void setFilterPolicy(IViewFilterPolicy filterPolicy) {
      this.filterPolicy = filterPolicy;
   }

   /**
    * @param projectPolicy
    *           the projectPolicy to set
    */
   public void setProjectPolicy(IViewProjectPolicy projectPolicy) {
      this.projectPolicy = projectPolicy;
   }

   /**
    * @return true if all policies have been set
    */
   public boolean validatePolicies() {
      return joinPolicy != null && filterPolicy != null
            && projectPolicy != null;
   }

   /**
    * Generate a number of queries for the provided schema based on the join,
    * filter, and project policies.
    * 
    * @param schema
    * @return
    */
   public List<View> generateViews(Schema schema, Query query) {

      List<View> queries = new ArrayList<View>(numViews);
      int count = 0;
      int numAttempts = 0;

      while (count < numViews && numAttempts < MAX_ATTEMPTS) {
    	 View view = new View(count + 1);
    	  
         if (!joinPolicy.createJoinConditions(schema, query, view)) {
            // Failed to create all requested join conditions. Try again
            ++numAttempts;
            continue;
         }

         if (!filterPolicy.createFilterPredicates(query, view)) {
            // Failed to project all requested filters. Try again
            ++numAttempts;
            continue;
         }

         if (!projectPolicy.createProjectionAttributes(query, view)) {
            // Failed to project all requested attributes. Try again
            ++numAttempts;
            continue;
         }

         if(!queries.contains(view)) {
             queries.add(view);
         }
         ++count;
      }

      return queries;
   }
}
