/**
 * 
 */
package uk.ac.ox.cs.pdq.workloadgen.policies.querygen;

import java.util.List;

import uk.ac.ox.cs.pdq.workloadgen.policies.Parameter;
import uk.ac.ox.cs.pdq.workloadgen.policies.Parameter.InvalidParameterException;
import uk.ac.ox.cs.pdq.workloadgen.query.Query;
import uk.ac.ox.cs.pdq.workloadgen.schema.AttrStats;
import uk.ac.ox.cs.pdq.workloadgen.schema.Attribute;
import uk.ac.ox.cs.pdq.workloadgen.schema.AttrStats.StatsType;

/**
 * A filter policy that generates a fixed number of filter predicates for a
 * query. In particular, it can generate some number of equi predicates and some
 * number of range predicates.
 * 
 * @author herodotos.herodotou
 */
public class QFilterPolicyFixed extends QFilterPolicy {

   private int numTotalFilters;
   private int maxNumEquiFilters;
   private int maxNumRangeFilters;

   // For range filters, the policy will generate a range that will fall between
   // a min and a max percent.
   private float minRangePercent;
   private float maxRangePercent;

   /**
    * Default constructor
    */
   public QFilterPolicyFixed() {
      super();
      this.numTotalFilters = 0;
      this.maxNumEquiFilters = 0;
      this.maxNumRangeFilters = 0;
      this.minRangePercent = 0f;
      this.maxRangePercent = 100f;
   }

   /*
    * (non-Javadoc)
    * 
    * @see cy.ac.cut.cs.workloadgen.policies.IPolicy#initialize(java.util.List)
    */
   @Override
   public boolean initialize(List<Parameter> params)
         throws InvalidParameterException {
      // Get the parameters
      for (Parameter param : params) {
         try {
            if (param.getName().equalsIgnoreCase("NumTotalFilters")) {
               numTotalFilters = Integer.parseInt(param.getValue());
            } else if (param.getName().equalsIgnoreCase("MaxNumEquiFilters")) {
               maxNumEquiFilters = Integer.parseInt(param.getValue());
            } else if (param.getName().equalsIgnoreCase("MaxNumRangeFilters")) {
               maxNumRangeFilters = Integer.parseInt(param.getValue());
            } else if (param.getName().equalsIgnoreCase("MinRangePercent")) {
               minRangePercent = Float.parseFloat(param.getValue());
            } else if (param.getName().equalsIgnoreCase("MaxRangePercent")) {
               maxRangePercent = Float.parseFloat(param.getValue());
            } else {
               throw new InvalidParameterException("Unexpected parameter name="
                     + param.getName());
            }
         } catch (NumberFormatException e) {
            throw new InvalidParameterException("Invalid parameter", e);
         }
      }

      // Validate the parameters
      return maxNumEquiFilters <= numTotalFilters
            && maxNumRangeFilters <= numTotalFilters && minRangePercent >= 0
            && maxRangePercent <= 100 && minRangePercent <= maxRangePercent;
   }

   /*
    * (non-Javadoc)
    * 
    * @see cy.ac.cut.cs.workloadgen.policies.querygen.IQueryFilterPolicy#
    * createFilterPredicates(cy.ac.cut.cs.workloadgen.query.Query)
    */
   @Override
   public boolean createFilterPredicates(Query query) {

      // Gather all filterable attributes
      List<Attribute> attrs = extractFilterableAttributes(query);

      // Generate the filter predicates
      int iter = 0;
      int countTotal = 0;
      int countEqui = 0;
      int countRange = 0;

      while (countTotal < numTotalFilters && iter < attrs.size()) {
         Attribute attr = attrs.get(iter);
         AttrStats stats = attr.getStats();

         // Generate an equi-filter predicate
         if (stats.getStatsType() == StatsType.ST_LIST
               && countEqui < maxNumEquiFilters) {

            if (addEquiFilterPredicate(query, attr)) {
               ++countTotal;
               ++countEqui;
            }
         }

         // Generate a range filter predicate
         if (stats.getStatsType() == StatsType.ST_RANGE
               && countRange < maxNumRangeFilters) {

            float rangePercent = random.nextFloat()
                  * (maxRangePercent - minRangePercent) + minRangePercent;
            if (addRangeFilterPredicate(query, attr, rangePercent)) {
               ++countTotal;
               ++countRange;
            }
         }

         ++iter;
      }

      return countTotal == numTotalFilters;
   }

}
