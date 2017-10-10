package uk.ac.ox.cs.pdq.datasources.services.policies;

import java.util.Properties;

import uk.ac.ox.cs.pdq.datasources.services.rest.RESTResponseEvent;

/**
 * Check whether the number of results for a given period has been exceeded,
 * waits or throws an exception if so.
 * 
 * @author Julien Leblay
 *
 */
public class ResultAllowance extends PeriodicalAllowance {
	
	/**
	 * Constructor for ResultAllowance.
	 * @param limit int
	 * @param period long
	 * @param wait boolean
	 */
	public ResultAllowance(int limit, long period, boolean wait) {
		super(limit, period, wait);
	}
	
	/**
	 * Constructor for ResultAllowance.
	 * @param properties Properties
	 */
	public ResultAllowance(Properties properties) {
		super(properties);
	}

	/**
	 * Copy.
	 *
	 * @return UsagePolicy
	 * @see uk.ac.ox.cs.pdq.datasources.services.policies.UsagePolicy#copy()
	 */
	@Override
	public UsagePolicy copy() {
		return new ResultAllowance(this.getLimit(), this.getPeriod(), this.isWait());
	}

	/**
	 * Gets the amount.
	 *
	 * @param event RESTResponseEvent
	 * @return int
	 */
	@Override
	protected int getAmount(RESTResponseEvent event) {
		return event.getOutput().size();
	}
}
