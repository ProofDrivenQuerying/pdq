package uk.ac.ox.cs.pdq.datasources.services.policies;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;

import uk.ac.ox.cs.pdq.datasources.AccessException;
import uk.ac.ox.cs.pdq.datasources.legacy.services.AccessEvent;

/**
 * This handler is required by the EventBus class which does not allow
 * subscribing method to throw exceptions.
 * 
 * @author Julien Leblay
 *
 */
public class UsageViolationExceptionHandler implements SubscriberExceptionHandler {

	@Override
	public void handleException(Throwable arg0, SubscriberExceptionContext arg1) {
		if (arg0 instanceof AccessException) {
			((AccessEvent) arg1.getEvent()).setUsageViolationMessage(arg0.getMessage());
		}
		arg0.printStackTrace();
	}

}
