/**
 * 
 */
package cy.ac.cut.cs.workloadgen.policies.querygen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cy.ac.cut.cs.workloadgen.policies.Parameter;
import cy.ac.cut.cs.workloadgen.policies.Parameter.InvalidParameterException;
import cy.ac.cut.cs.workloadgen.query.JoinPredicate;
import cy.ac.cut.cs.workloadgen.query.Query;
import cy.ac.cut.cs.workloadgen.schema.Attribute;
import cy.ac.cut.cs.workloadgen.schema.Table;

/**
 * A projection policy that will project a specified percent of primary keys of
 * the tables that appear in the query.
 * 
 * @author herodotos.herodotou
 */
public class QProjectPolicyKeys implements IQueryProjectPolicy {

   private float percent;

   /**
    * Default constructor
    */
   public QProjectPolicyKeys() {
      super();
      this.percent = 100;
   }

   /*
    * (non-Javadoc)
    * 
    * @see cy.ac.cut.cs.workloadgen.policies.IPolicy#initialize(java.util.List)
    */
   @Override
   public boolean initialize(List<Parameter> params)
         throws InvalidParameterException {
      // Get the two parameters
      for (Parameter param : params) {
         try {
            if (param.getName().equalsIgnoreCase("Percent")) {
               percent = Float.parseFloat(param.getValue());
            } else {
               throw new InvalidParameterException("Unexpected parameter name="
                     + param.getName());
            }
         } catch (NumberFormatException e) {
            throw new InvalidParameterException("Invalid parameter", e);
         }
      }

      // Validate the parameters
      return percent >= 0 && percent <= 100;
   }

   /*
    * (non-Javadoc)
    * 
    * @see cy.ac.cut.cs.workloadgen.policies.querygen.IQueryProjectPolicy#
    * createProjectionAttributes(cy.ac.cut.cs.workloadgen.query.Query)
    */
   @Override
   public boolean createProjectionAttributes(Query query) {

      // Gather all keys
      ArrayList<Attribute> attrs = new ArrayList<Attribute>();
      for (Table table : query.getFromClause()) {
         for (Attribute attr : table.getPrimaryKey()) {
            attrs.add(attr);
         }
      }
      
      //If two keys join ensure that you are projecting only one of these
      for(JoinPredicate predicate:query.getWhereClause().getJoinPredicates()) {
    	  if(attrs.contains(predicate.getAttr1()) && attrs.contains(predicate.getAttr2())) {
    		  attrs.remove(predicate.getAttr2());
    	  }
      }
      

      // Get the number of attributes to project
      int numAttrs = Math.round(attrs.size() * percent / 100f);
      numAttrs = numAttrs == 0 ? 1 : numAttrs;

      // Project a random subset of the attributes
      if (numAttrs != attrs.size())
         Collections.shuffle(attrs);

      for (int i = 0; i < numAttrs; ++i) {
         query.addProjectAttribute(attrs.get(i));
      }

      return true;
   }

}
