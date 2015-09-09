package uk.ac.ox.cs.pdq.services;

import com.google.common.eventbus.Subscribe;


/**
 * AccessPostProcessor event handler, that is triggered after an access was performed.
 * 
 * @author Julien Leblay
 * 
 */
public interface AccessPostProcessor<T extends ResponseEvent> {
	/**
	 * Method called upon an access ResponseEvent
	 * @param event
	 */
	@Subscribe
	void processAccessResponse(T event) ;
}
