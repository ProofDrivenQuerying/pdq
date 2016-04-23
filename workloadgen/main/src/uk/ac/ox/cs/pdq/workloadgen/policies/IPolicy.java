/**
 * 
 */
package uk.ac.ox.cs.pdq.workloadgen.policies;

import java.util.List;

import uk.ac.ox.cs.pdq.workloadgen.policies.Parameter.InvalidParameterException;

/**
 * Interface for a policy
 * 
 * @author herodotos.herodotou
 */
public interface IPolicy {

   /**
    * Initialize the policy with custom parameter values.
    * 
    * @param params
    * @return true if all required parameters have been initialized correctly
    * @throws InvalidParameterException
    *            when a parameter name or value is invalid
    */
   public boolean initialize(List<Parameter> params)
         throws InvalidParameterException;
}
