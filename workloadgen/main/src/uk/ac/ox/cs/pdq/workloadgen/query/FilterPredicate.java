/**
 * 
 */
package uk.ac.ox.cs.pdq.workloadgen.query;

import java.text.SimpleDateFormat;

import uk.ac.ox.cs.pdq.workloadgen.schema.Attribute;

/**
 * Represents a filter predicate of the form 'Attr Op Value'
 * 
 * @author herodotos.herodotou
 */
public class FilterPredicate {

   private static SimpleDateFormat DateFormatter = new SimpleDateFormat(
         "yyyy-MM-dd");

   private Attribute attribute;
   private Operator op;
   private Object value;

   /**
    * @param attribute
    * @param op
    * @param value
    */
   public FilterPredicate(Attribute attribute, Operator op, Object value) {
      this.attribute = attribute;
      this.op = op;
      this.value = value;
   }

   /**
    * @return the attribute
    */
   public Attribute getAttribute() {
      return attribute;
   }

   /**
    * @return the op
    */
   public Operator getOp() {
      return op;
   }

   /**
    * @return the value
    */
   public Object getValue() {
      return value;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      String strValue;
      switch (attribute.getType()) {
      case AT_CHAR:
      case AT_STRING:
         strValue = "'" + value.toString() + "'";
         break;
      case AT_DATE:
         strValue = "DATE '" + DateFormatter.format(value) + "'";
         break;
      case AT_DOUBLE:
         double d = Math.abs((Double) value);
         int n = (d >= 1 || d == 0) ? 3 : (int) Math.ceil(-Math.log10(d)) + 2;
         strValue = String.format("%." + n + "f", value);
         break;
      case AT_INTEGER:
      case AT_UNKNOWN:
      default:
         strValue = value.toString();
         break;
      }

      return attribute.toString() + " " + op.toString() + " " + strValue;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + attribute.hashCode();
      result = 31 * result + op.hashCode();
      result = 31 * result + value.hashCode();
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
      if (!(obj instanceof FilterPredicate))
         return false;
      FilterPredicate other = (FilterPredicate) obj;
      if (!attribute.equals(other.attribute))
         return false;
      if (op != other.op)
         return false;
      if (!value.equals(other.value))
         return false;
      return true;
   }

   /**
    * Represents a comparison operator for a filter predicate.
    */
   public enum Operator {

      CO_EQUALS, // =
      CO_NOT_EQUALS, // <>
      CO_LESS_THAN, // <
      CO_LESS_THAN_EQUALS, // <=
      CO_GREATER_THAN, // >
      CO_GREATER_THAN_EQUALS; // >=

      /*
       * (non-Javadoc)
       * 
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString() {
         switch (this) {
         case CO_EQUALS:
            return "=";
         case CO_NOT_EQUALS:
            return "<>";
         case CO_LESS_THAN:
            return "<";
         case CO_LESS_THAN_EQUALS:
            return "<=";
         case CO_GREATER_THAN:
            return ">";
         case CO_GREATER_THAN_EQUALS:
            return ">=";
         default:
            return "UNKNOWN";
         }
      }

   }

}