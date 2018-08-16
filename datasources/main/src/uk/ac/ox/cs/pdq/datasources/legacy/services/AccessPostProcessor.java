package uk.ac.ox.cs.pdq.datasources.legacy.services;


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
	void processAccessResponse(T event) ;
}
