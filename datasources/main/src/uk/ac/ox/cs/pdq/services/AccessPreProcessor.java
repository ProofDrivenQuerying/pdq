package uk.ac.ox.cs.pdq.services;

import com.google.common.eventbus.Subscribe;


/**
 * AccessPreProcessor event handler, that is triggered after an access was performed.
 * 
 * @author Julien Leblay
 * 
 */
public interface AccessPreProcessor<T extends RequestEvent> {
	/**
	 * Method called upon an access RequestEvent
	 * @param event
	 */
	@Subscribe
	void processAccessRequest(T event) ;
}
