/**
 * 
 */
package uk.ac.ox.cs.pdq.workloadgen.schema;

import uk.ac.ox.cs.pdq.workloadgen.schema.Attribute.AttrType;

/**
 * Represents a range of values an attribute can take.
 * 
 * @author herodotos.herodotou
 */
public class AttrStatsRange extends AttrStats {

   private Object min;
   private Object max;

   /**
    * @param attrType
    *           attribute type
    * @param min
    *           min value of the same type
    * @param max
    *           max value of the same type
    */
   public AttrStatsRange(AttrType attrType, Object min, Object max) {
      super(attrType, StatsType.ST_RANGE);
      this.min = min;
      this.max = max;
   }

   /**
    * @return the min
    */
   public Object getMin() {
      return min;
   }

   /**
    * @return the max
    */
   public Object getMax() {
      return max;
   }

}
