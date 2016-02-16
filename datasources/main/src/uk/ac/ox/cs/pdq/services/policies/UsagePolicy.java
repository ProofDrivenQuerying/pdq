package uk.ac.ox.cs.pdq.services.policies;




// TODO: Auto-generated Javadoc
/**
 * Usage policy for web resources. Implemented this class to model ad-hoc usage
 * policy that web services put in places (e.g. data allowance, bandwidth
 * limits, authentication, etc.)
 * 
 * @author Julien Leblay
 * 
 */
public interface UsagePolicy {

	/**
	 * Copy.
	 *
	 * @return a field-to-field copy of the usage policy
	 */
	UsagePolicy copy();
}
