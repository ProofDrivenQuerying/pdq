package uk.ac.ox.cs.pdq.services;

import com.google.common.eventbus.Subscribe;


// TODO: Auto-generated Javadoc
/**
 * AccessPostProcessor event handler, that is triggered after an access was performed.
 *
 * @author Julien Leblay
 * @param <T> the generic type
 */
public interface AccessPostProcessor<T extends ResponseEvent> {
	
	/**
	 * Method called upon an access ResponseEvent.
	 *
	 * @param event the event
	 */
	@Subscribe
	void processAccessResponse(T event) ;
}
