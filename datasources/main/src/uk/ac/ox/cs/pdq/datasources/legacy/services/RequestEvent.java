package uk.ac.ox.cs.pdq.datasources.legacy.services;

import uk.ac.ox.cs.pdq.datasources.AccessException;


/**
 * An access's request event.
 *
 * @author Julien Leblay
 */
public interface RequestEvent extends AccessEvent {
	
	/**
	 * Process request.
	 *
	 * @return ResponseEvent
	 * @throws AccessException the access exception
	 */
	ResponseEvent processRequest() throws AccessException;
}
