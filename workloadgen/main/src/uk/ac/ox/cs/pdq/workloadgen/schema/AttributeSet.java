/**
 * 
 */
package uk.ac.ox.cs.pdq.workloadgen.schema;

import java.util.List;

/**
 * Represents an ordered set of attributes from the same table.
 * 
 * @author herodotos.herodotou
 */
public class AttributeSet {

   private Table table;
   private List<Attribute> attributes;

   /**
    * @param table
    * @param attributes
    *           expected to belong to the same table
    */
   public AttributeSet(Table table, List<Attribute> attributes) {
      this.table = table;
      this.attributes = attributes;
   }

   /**
    * @return the table
    */
   public Table getTable() {
      return table;
   }

   /**
    * @return the attributes
    */
   public List<Attribute> getAttributes() {
      return attributes;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return attributes.toString();
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + table.hashCode();
      result = 31 * result + attributes.hashCode();
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof AttributeSet))
         return false;
      AttributeSet other = (AttributeSet) obj;
      if (!attributes.equals(other.attributes))
         return false;
      if (!table.equals(other.table))
         return false;
      return true;
   }

}
