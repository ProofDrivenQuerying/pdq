package uk.ac.ox.cs.pdq.services.policies;

import uk.ac.ox.cs.pdq.AccessException;
import uk.ac.ox.cs.pdq.services.AccessEvent;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;

// TODO: Auto-generated Javadoc
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
