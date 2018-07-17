package uk.ac.ox.cs.pdq.datasources.legacy.services.policies;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;

import uk.ac.ox.cs.pdq.datasources.legacy.services.AccessEvent;
import uk.ac.ox.cs.pdq.datasources.resultstable.AccessException;

/**
 * This handler is required by the EventBus class which does not allow
 * subscribing method to throw exceptions.
 * 
 * @author Julien Leblay
 *
 */
public class UsageViolationExceptionHandler implements SubscriberExceptionHandler {

	/*
	 * (non-Javadoc)
	 * @see com.google.common.eventbus.SubscriberExceptionHandler#handleException(java.lang.Throwable, com.google.common.eventbus.SubscriberExceptionContext)
	 */
	@Override
	public void handleException(Throwable arg0, SubscriberExceptionContext arg1) {
		if (arg0 instanceof AccessException) {
			((AccessEvent) arg1.getEvent()).setUsageViolationMessage(arg0.getMessage());
		}
		arg0.printStackTrace();
	}

}
