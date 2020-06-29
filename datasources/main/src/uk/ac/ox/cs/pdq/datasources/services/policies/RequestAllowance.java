// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.services.policies;

import java.util.Properties;

import uk.ac.ox.cs.pdq.datasources.legacy.services.rest.RESTResponseEvent;
import uk.ac.ox.cs.pdq.datasources.services.servicegroup.GroupUsagePolicy;

/**
 * Check whether the number of requests for a given period is satisfied, waits
 * or throws an exception if not.
 * 
 * @author Julien Leblay
 *
 */
public class RequestAllowance extends PeriodicalAllowance {
	
	public RequestAllowance(int limit, long period, boolean wait) {
		super(limit, period, wait);
	}
	
	public RequestAllowance(Properties properties) {
		super(properties);
	}

	public RequestAllowance(GroupUsagePolicy gup) {
		super(gup);
	}

	@Override
	public UsagePolicy copy() {
		return new RequestAllowance(this.getLimit(), this.getPeriod(), this.isWait());
	}

	@Override
	protected int getAmount(RESTResponseEvent event) {
		return 1;
	}
}
