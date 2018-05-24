package uk.ac.ox.cs.pdq.datasources.services;

import com.google.common.eventbus.Subscribe;


/**
 * AccessPostProcessor event handler, that is triggered after an access was performed.
 *
 * @author Julien Leblay
 * @param <T> the generic type
 */
public interface AccessPostProcessor<T> {
	
	/**
	 * Method called upon an access ResponseEvent.
	 *
	 * @param event the event
	 */
	@Subscribe
	void processAccessResponse(T event) ;
}
