/**
 * 
 */
package cy.ac.cut.cs.workloadgen.schema;

/**
 * Represents two attributes from two different tables that can participate in
 * an equi-join. The order of the two attributes is not important.
 * 
 * @author herodotos.herodotou
 */
public class JoinableKey {

   private Attribute attr1;
   private Attribute attr2;

   /**
    * @param attr1
    * @param attr2
    */
   public JoinableKey(Attribute attr1, Attribute attr2) {
      this.attr1 = attr1;
      this.attr2 = attr2;
   }

   /**
    * @return the attr1
    */
   public Attribute getAttr1() {
      return attr1;
   }

   /**
    * @return the attr2
    */
   public Attribute getAttr2() {
      return attr2;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return "JK: " + attr1 + " <-> " + attr2;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      // Order of the two attributes is not important
      return attr1.hashCode() + attr2.hashCode();
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
      if (!(obj instanceof JoinableKey))
         return false;
      JoinableKey other = (JoinableKey) obj;

      // Order of the two attributes is not important
      if ((attr1.equals(other.attr1) && attr2.equals(other.attr2))
            || (attr1.equals(other.attr2) && attr2.equals(other.attr1)))
         return true;
      else
         return false;
   }

}
