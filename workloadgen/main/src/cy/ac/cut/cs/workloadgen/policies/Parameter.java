/**
 * 
 */
package cy.ac.cut.cs.workloadgen.policies;

/**
 * Represents a name/value parameter of a policy
 * 
 * @author herodotos.herodotou
 */
public class Parameter {

   private String name;
   private String value;

   /**
    * @param name
    * @param value
    */
   public Parameter(String name, String value) {
      this.name = name;
      this.value = value;
   }

   /**
    * @return the name
    */
   public String getName() {
      return name;
   }

   /**
    * @return the value
    */
   public String getValue() {
      return value;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return "[" + name + "," + value + "]";
   }

   /**
    * Custom parameter exception
    * 
    * @author herodotos.herodotou
    */
   public static class InvalidParameterException extends Exception {

      private static final long serialVersionUID = -2304024474373761786L;

      public InvalidParameterException(String message) {
         super(message);
      }

      public InvalidParameterException(String message, Throwable cause) {
         super(message, cause);
      }
   }

}
