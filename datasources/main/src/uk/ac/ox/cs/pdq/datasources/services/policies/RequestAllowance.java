package uk.ac.ox.cs.pdq.datasources.services.policies;

import java.util.Properties;

import uk.ac.ox.cs.pdq.datasources.legacy.services.rest.RESTResponseEvent;

/**
 * Check whether the number of requests for a given period is satisfied, waits
 * or throws an exception if not.
 * 
 * @author Julien Leblay
 *
 */
public class RequestAllowance extends PeriodicalAllowance {
	
	/**
	 * Constructor for RequestAllowance.
	 * @param limit int
	 * @param period long
	 * @param wait boolean
	 */
	public RequestAllowance(int limit, long period, boolean wait) {
		super(limit, period, wait);
	}
	
	/**
	 * Constructor for RequestAllowance.
	 * @param properties Properties
	 */
	public RequestAllowance(Properties properties) {
		super(properties);
	}

	/**
	 * Copy.
	 *
	 * @return UsagePolicy
	 * @see uk.ac.ox.cs.pdq.datasources.io.jaxb.servicegroup.GroupUsagePolicy.UsagePolicy#copy()
	 */
	@Override
	public UsagePolicy copy() {
		return new RequestAllowance(this.getLimit(), this.getPeriod(), this.isWait());
	}

	/**
	 *
	 * @param event RESTResponseEvent
	 * @return int
	 */
	@Override
	protected int getAmount(RESTResponseEvent event) {
		return 1;
	}
}
