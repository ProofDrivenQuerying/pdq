/**
 * 
 */
package cy.ac.cut.cs.workloadgen.policies.querygen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import cy.ac.cut.cs.workloadgen.policies.Parameter;
import cy.ac.cut.cs.workloadgen.policies.Parameter.InvalidParameterException;
import cy.ac.cut.cs.workloadgen.query.FilterPredicate;
import cy.ac.cut.cs.workloadgen.query.Query;
import cy.ac.cut.cs.workloadgen.query.FilterPredicate.Operator;
import cy.ac.cut.cs.workloadgen.schema.AttrStats;
import cy.ac.cut.cs.workloadgen.schema.AttrStatsList;
import cy.ac.cut.cs.workloadgen.schema.AttrStatsRange;
import cy.ac.cut.cs.workloadgen.schema.Attribute;
import cy.ac.cut.cs.workloadgen.schema.Table;
import cy.ac.cut.cs.workloadgen.schema.AttrStats.StatsType;
import cy.ac.cut.cs.workloadgen.schema.Attribute.AttrType;

/**
 * Base class for a policy that generates filter conditions for a query. This
 * class provided some useful methods that might be common to several different
 * policies.
 * 
 * @author herodotos.herodotou
 */
public abstract class QFilterPolicy implements IQueryFilterPolicy {

   protected static Random random = new Random();

   /*
    * (non-Javadoc)
    * 
    * @see cy.ac.cut.cs.workloadgen.policies.IPolicy#initialize(java.util.List)
    */
   @Override
   public abstract boolean initialize(List<Parameter> params)
         throws InvalidParameterException;

   /*
    * (non-Javadoc)
    * 
    * @see cy.ac.cut.cs.workloadgen.policies.querygen.IQueryFilterPolicy#
    * createFilterPredicates(cy.ac.cut.cs.workloadgen.query.Query)
    */
   @Override
   public abstract boolean createFilterPredicates(Query query);

   /**
    * Collect all filterable attributes that have associated statistics from the
    * query's from clause
    * 
    * @param query
    * @return list of attributes
    */
   protected List<Attribute> extractFilterableAttributes(Query query) {
      List<Attribute> attrs = new ArrayList<Attribute>();
      for (Table table : query.getFromClause()) {
         for (Attribute attr : table.getFilterableAttrs()) {
            if (attr.hasStats()) {
               attrs.add(attr);
            }
         }
      }

      Collections.shuffle(attrs);

      return attrs;
   }

   /**
    * Create and add an equi-filter predicate by randomly selecting one value
    * from a list of attribute values.
    * 
    * Applies only to attributes with stats type = ST_LIST
    * 
    * @param query
    * @param attr
    * @return true if added successfully
    */
   protected boolean addEquiFilterPredicate(Query query, Attribute attr) {

      AttrStats stats = attr.getStats();
      if (stats.getStatsType() != StatsType.ST_LIST)
         return false;

      // Generate an equi-filter predicate
      AttrStatsList statsList = (AttrStatsList) stats;
      int v = random.nextInt(statsList.getValues().size());
      FilterPredicate filter = new FilterPredicate(attr, Operator.CO_EQUALS,
            statsList.getValues().get(v));

      return query.addFilterPredicate(filter);
   }

   /**
    * Create and add a range filter predicate based on a range of attribute
    * values
    * 
    * Applies only to attributes with stats type = ST_RANGE
    * 
    * @param query
    * @param attr
    * @param rangePercent
    * @return
    */
   protected boolean addRangeFilterPredicate(Query query, Attribute attr,
         float rangePercent) {

      AttrStats stats = attr.getStats();
      if (stats.getStatsType() != StatsType.ST_RANGE)
         return false;

      boolean success = false;
      switch (stats.getAttrType()) {
      case AT_INTEGER:
         success = addIntegerRangeFilterPredicate(query, attr, rangePercent);
         break;
      case AT_DOUBLE:
         success = addDoubleRangeFilterPredicate(query, attr, rangePercent);
         break;
      case AT_DATE:
         success = addDateRangeFilterPredicate(query, attr, rangePercent);
         break;
      default:
         success = false;
         break;
      }

      return success;
   }

   /**
    * Create and add a range filter predicate based on an integer range of
    * values
    * 
    * Example: If asked to create a 20% range between 50 and 100, this method
    * will randomly generate a range with width 10 between 50 and 100.
    * 
    * @param query
    * @param attr
    * @param rangePercent
    * @return true if the filter predicates are added correctly.
    */
   private boolean addIntegerRangeFilterPredicate(Query query, Attribute attr,
         float rangePercent) {

      AttrStats stats = attr.getStats();
      if (stats.getStatsType() != StatsType.ST_RANGE
            || stats.getAttrType() != AttrType.AT_INTEGER)
         return false;

      // Generate a custom range
      AttrStatsRange statsRange = (AttrStatsRange) stats;
      int min = (Integer) statsRange.getMin();
      int max = (Integer) statsRange.getMax();

      int rangeAbs = (int) Math.floor(rangePercent * (max - min) / 100);

      int lowerBound = random.nextInt(max - rangeAbs - min) + min;
      int upperBound = lowerBound + rangeAbs;

      // Create the filter predicates
      FilterPredicate lowerFilter = new FilterPredicate(attr,
            Operator.CO_GREATER_THAN, lowerBound);
      FilterPredicate upperFilter = new FilterPredicate(attr,
            Operator.CO_LESS_THAN_EQUALS, upperBound);

      // Add the filter predicates
      return query.addFilterPredicate(lowerFilter)
            && query.addFilterPredicate(upperFilter);
   }

   /**
    * Create and add a range filter predicate based on a range of double values.
    * 
    * Example: If asked to create a 20% range between 50.0 and 100.0, this
    * method will randomly generate a range with width 10 between 50.0 and
    * 100.0.
    * 
    * @param query
    * @param attr
    * @param rangePercent
    * @return true if the filter predicates are added correctly.
    */
   private boolean addDoubleRangeFilterPredicate(Query query, Attribute attr,
         float rangePercent) {

      AttrStats stats = attr.getStats();
      if (stats.getStatsType() != StatsType.ST_RANGE
            || stats.getAttrType() != AttrType.AT_DOUBLE)
         return false;

      // Generate a custom range
      AttrStatsRange statsRange = (AttrStatsRange) stats;
      double min = (Double) statsRange.getMin();
      double max = (Double) statsRange.getMax();

      double rangeAbs = rangePercent * (max - min) / 100f;

      double lowerBound = random.nextDouble() * (max - rangeAbs - min) + min;
      double upperBound = lowerBound + rangeAbs;

      // Create the filter predicates
      FilterPredicate lowerFilter = new FilterPredicate(attr,
            Operator.CO_GREATER_THAN, lowerBound);
      FilterPredicate upperFilter = new FilterPredicate(attr,
            Operator.CO_LESS_THAN_EQUALS, upperBound);

      // Add the filter predicates
      return query.addFilterPredicate(lowerFilter)
            && query.addFilterPredicate(upperFilter);
   }

   /**
    * Create and add a range filter predicate based on a range of double values.
    * 
    * Example: If asked to create a 20% range between 50.0 and 100.0, this
    * method will randomly generate a range with width 10 between 50.0 and
    * 100.0.
    * 
    * @param query
    * @param attr
    * @param rangePercent
    * @return true if the filter predicates are added correctly.
    */
   private boolean addDateRangeFilterPredicate(Query query, Attribute attr,
         float rangePercent) {

      AttrStats stats = attr.getStats();
      if (stats.getStatsType() != StatsType.ST_RANGE
            || stats.getAttrType() != AttrType.AT_DATE)
         return false;

      // Generate a custom range
      AttrStatsRange statsRange = (AttrStatsRange) stats;
      long min = ((Date) statsRange.getMin()).getTime();
      long max = ((Date) statsRange.getMax()).getTime();

      long rangeAbs = (long) Math.floor(rangePercent * (max - min) / 100);

      long lowerBound = ((long) random.nextDouble() * (max - rangeAbs - min))
            + min;
      long upperBound = lowerBound + rangeAbs;

      // Create the filter predicates
      FilterPredicate lowerFilter = new FilterPredicate(attr,
            Operator.CO_GREATER_THAN, new Date(lowerBound));
      FilterPredicate upperFilter = new FilterPredicate(attr,
            Operator.CO_LESS_THAN_EQUALS, new Date(upperBound));

      // Add the filter predicates
      return query.addFilterPredicate(lowerFilter)
            && query.addFilterPredicate(upperFilter);
   }

}
