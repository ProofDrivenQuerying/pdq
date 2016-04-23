/**
 * 
 */
package cy.ac.cut.cs.workloadgen.policies.viewgen;

import java.util.List;
import java.util.Random;

import cy.ac.cut.cs.workloadgen.policies.Parameter;
import cy.ac.cut.cs.workloadgen.policies.Parameter.InvalidParameterException;
import cy.ac.cut.cs.workloadgen.query.Query;
import cy.ac.cut.cs.workloadgen.query.View;

/**
 * Base class for a policy that generates filter conditions for a query. This
 * class provided some useful methods that might be common to several different
 * policies.
 * 
 * @author herodotos.herodotou
 */
public abstract class VFilterPolicy implements IViewFilterPolicy {

   protected static Random random = new Random();

   /*
    * (non-Javadoc)
    * 
    * @see cy.ac.cut.cs.workloadgen.policies.IPolicy#initialize(java.util.List)
    */
   @Override
   public abstract boolean initialize(List<Parameter> params)
         throws InvalidParameterException;

   /*
    * (non-Javadoc)
    * 
    * @see cy.ac.cut.cs.workloadgen.policies.querygen.IQueryFilterPolicy#
    * createFilterPredicates(cy.ac.cut.cs.workloadgen.query.Query)
    */
   @Override
   public abstract boolean createFilterPredicates(Query query, View view);

}
