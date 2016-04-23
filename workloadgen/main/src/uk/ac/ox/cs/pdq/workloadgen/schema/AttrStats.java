/**
 * 
 */
package uk.ac.ox.cs.pdq.workloadgen.schema;

import uk.ac.ox.cs.pdq.workloadgen.schema.Attribute.AttrType;

/**
 * Base class for representing statistics/information about the set of values an
 * attribute can take.
 * 
 * @author herodotos.herodotou
 */
public abstract class AttrStats {

   private AttrType attrType;
   private StatsType statsType;

   /**
    * @param attrType
    * @param statsTtype
    */
   public AttrStats(AttrType attrType, StatsType statsTtype) {
      this.attrType = attrType;
      this.statsType = statsTtype;
   }

   /**
    * @return the attrType
    */
   public AttrType getAttrType() {
      return attrType;
   }

   /**
    * @return the statsType
    */
   public StatsType getStatsType() {
      return statsType;
   }

   /**
    * Enumeration of attribute statistics types
    * 
    * @author herodotos.herodotou
    */
   public enum StatsType {
      ST_LIST, // list of values
      ST_RANGE; // range of values
   }
}
