/**
 * 
 */
package cy.ac.cut.cs.workloadgen.schema;

/**
 * Represents a foreign key: an ordered set of attributes from the referencing
 * table reference the primary key (another ordered set of attributes) of the
 * referenced table.
 * 
 * @author herodotos.herodotou
 */
public class ForeignKey {

   private AttributeSet referencing;
   private AttributeSet referenced;

   /**
    * @param referencing
    * @param referenced
    */
   public ForeignKey(AttributeSet referencing, AttributeSet referenced) {
      this.referencing = referencing;
      this.referenced = referenced;
   }

   /**
    * @return the referencing
    */
   public AttributeSet getReferencingSet() {
      return referencing;
   }

   /**
    * @return the referenced
    */
   public AttributeSet getReferencedSet() {
      return referenced;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return "FK: " + referencing + " -> " + referenced;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + referenced.hashCode();
      result = 31 * result + referencing.hashCode();
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
      if (!(obj instanceof ForeignKey))
         return false;
      ForeignKey other = (ForeignKey) obj;
      if (!referenced.equals(other.referenced))
         return false;
      if (!referencing.equals(other.referencing))
         return false;
      return true;
   }

}
