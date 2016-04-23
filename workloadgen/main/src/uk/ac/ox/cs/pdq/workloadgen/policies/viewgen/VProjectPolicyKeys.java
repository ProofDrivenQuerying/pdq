/**
 * 
 */
package uk.ac.ox.cs.pdq.workloadgen.policies.viewgen;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import uk.ac.ox.cs.pdq.workloadgen.policies.Parameter;
import uk.ac.ox.cs.pdq.workloadgen.policies.Parameter.InvalidParameterException;
import uk.ac.ox.cs.pdq.workloadgen.query.Query;
import uk.ac.ox.cs.pdq.workloadgen.query.View;
import uk.ac.ox.cs.pdq.workloadgen.schema.Attribute;

import com.google.common.collect.Lists;

/**
 * A projection policy that will project a specified percent of primary keys of
 * the tables that appear in the query.
 * 
 * @author herodotos.herodotou
 */
public class VProjectPolicyKeys implements IViewProjectPolicy {

   private float percent;
   private Random random;

   /**
    * Default constructor
    */
   public VProjectPolicyKeys() {
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
   public boolean createProjectionAttributes(Query query, View view) {

	   this.random = new Random(query.getSelectClause().size() + query.getId());
      // Gather all projected attributes 
      List<Attribute> attrs = Lists.newArrayList();
      for(Attribute attribute: query.getSelectClause()) {
    	  if(view.getFromClause().contains(attribute.getTable())) {
    		  attrs.add(attribute);
    	  }
      }
      if(attrs.isEmpty()) {
    	  return false;
      }

      // Get the number of attributes to project
      int numAttrs = Math.round(attrs.size() * this.random.nextFloat() * 100 / 100f);
      numAttrs = numAttrs == 0 ? 1 : numAttrs;

      // Project a random subset of the attributes
      if (numAttrs != attrs.size())
         Collections.shuffle(attrs);

      for (int i = 0; i < numAttrs; ++i) {
         view.addProjectAttribute(attrs.get(i));
      }

      return true;
   }

}
