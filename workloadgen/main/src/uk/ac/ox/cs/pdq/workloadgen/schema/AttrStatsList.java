/**
 * 
 */
package uk.ac.ox.cs.pdq.workloadgen.schema;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.workloadgen.schema.Attribute.AttrType;

/**
 * Represents a list of values an attribute can take.
 * 
 * @author herodotos.herodotou
 */
public class AttrStatsList extends AttrStats {

   private List<Object> values;

   /**
    * @param attrType
    *           attribute type
    * @param values
    *           list of values of the same type
    */
   public AttrStatsList(AttrType attrType, List<Object> values) {
      super(attrType, StatsType.ST_LIST);
      this.values = new ArrayList<Object>(values);
   }

   /**
    * @return the values
    */
   public List<Object> getValues() {
      return values;
   }

}
