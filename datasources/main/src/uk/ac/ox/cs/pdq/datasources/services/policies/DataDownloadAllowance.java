package uk.ac.ox.cs.pdq.datasources.services.policies;

import java.util.Properties;

import uk.ac.ox.cs.pdq.datasources.io.jaxb.servicegroup.GroupUsagePolicy;
import uk.ac.ox.cs.pdq.datasources.legacy.services.rest.RESTResponseEvent;

/**
 * Check whether the amount of data for a given period has exceeded, waits
 * or throws an exception if so.
 * 
 * @author Julien Leblay
 *
 */
public class DataDownloadAllowance extends PeriodicalAllowance {
	
	public DataDownloadAllowance(int limit, long period, boolean wait) {
		super(limit, period, wait);
	}
	
	public DataDownloadAllowance(Properties properties) {
		super(properties);
	}

	public DataDownloadAllowance(GroupUsagePolicy gup) {
		super(gup);
	}

	@Override
	public UsagePolicy copy() {
		return new DataDownloadAllowance(this.getLimit(), this.getPeriod(), this.isWait());
	}

	@Override
	protected int getAmount(RESTResponseEvent event) {
		return event.getResponse().getLength();
	}
}
