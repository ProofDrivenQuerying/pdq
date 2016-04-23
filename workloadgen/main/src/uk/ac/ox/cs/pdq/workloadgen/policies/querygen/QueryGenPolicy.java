/**
 * 
 */
package uk.ac.ox.cs.pdq.workloadgen.policies.querygen;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.workloadgen.query.Query;
import uk.ac.ox.cs.pdq.workloadgen.schema.Schema;

/**
 * High-level query generation policy that uses the join, filter, and project
 * policies for constructing queries.
 * 
 * @author herodotos.herodotou
 */
public class QueryGenPolicy {

   private int numQueries;
   private IQueryJoinPolicy joinPolicy;
   private IQueryFilterPolicy filterPolicy;
   private IQueryProjectPolicy projectPolicy;

   private static int MAX_ATTEMPTS = 100;

   /**
    * @param numQueries
    */
   public QueryGenPolicy(int numQueries) {
      this.numQueries = numQueries;
      this.joinPolicy = null;
      this.filterPolicy = null;
      this.projectPolicy = null;
   }

   /**
    * @param joinPolicy
    *           the joinPolicy to set
    */
   public void setJoinPolicy(IQueryJoinPolicy joinPolicy) {
      this.joinPolicy = joinPolicy;
   }

   /**
    * @param filterPolicy
    *           the filterPolicy to set
    */
   public void setFilterPolicy(IQueryFilterPolicy filterPolicy) {
      this.filterPolicy = filterPolicy;
   }

   /**
    * @param projectPolicy
    *           the projectPolicy to set
    */
   public void setProjectPolicy(IQueryProjectPolicy projectPolicy) {
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
   public List<Query> generateQueries(Schema schema) {

      List<Query> queries = new ArrayList<Query>(numQueries);
      int count = 0;
      int numAttempts = 0;

      while (count < numQueries && numAttempts < MAX_ATTEMPTS) {
         Query query = new Query(count + 1);

         if (!joinPolicy.createJoinConditions(schema, query)) {
            // Failed to create all requested join conditions. Try again
            ++numAttempts;
            continue;
         }

         if (!filterPolicy.createFilterPredicates(query)) {
            // Failed to project all requested filters. Try again
            ++numAttempts;
            continue;
         }

         if (!projectPolicy.createProjectionAttributes(query)) {
            // Failed to project all requested attributes. Try again
            ++numAttempts;
            continue;
         }

         queries.add(query);
         ++count;
      }

      return queries;
   }
}
