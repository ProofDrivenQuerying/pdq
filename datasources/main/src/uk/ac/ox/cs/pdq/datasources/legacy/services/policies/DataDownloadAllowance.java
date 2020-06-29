// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.legacy.services.policies;

import java.util.Properties;

import uk.ac.ox.cs.pdq.datasources.legacy.services.rest.RESTResponseEvent;

/**
 * Check whether the amount of data for a given period has exceeded, waits
 * or throws an exception if so.
 * 
 * @author Julien Leblay
 *
 */
public class DataDownloadAllowance extends PeriodicalAllowance {
	
	/**
	 * Constructor for DataDownloadAllowance.
	 * @param limit int
	 * @param period long
	 * @param wait boolean
	 */
	public DataDownloadAllowance(int limit, long period, boolean wait) {
		super(limit, period, wait);
	}
	
	/**
	 * Constructor for DataDownloadAllowance.
	 * @param properties Properties
	 */
	public DataDownloadAllowance(Properties properties) {
		super(properties);
	}

	/**
	 *
	 * @return UsagePolicy
	 * @see uk.ac.ox.cs.pdq.datasources.io.jaxb.servicegroup.policies.UsagePolicy#copy()
	 */
	@Override
	public UsagePolicy copy() {
		return new DataDownloadAllowance(this.getLimit(), this.getPeriod(), this.isWait());
	}

	/**
	 *
	 * @param event RESTResponseEvent
	 * @return int
	 */
	@Override
	protected int getAmount(RESTResponseEvent event) {
		return event.getResponse().getLength();
	}
}
