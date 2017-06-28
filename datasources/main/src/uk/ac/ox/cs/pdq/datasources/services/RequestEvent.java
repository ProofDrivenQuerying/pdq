package uk.ac.ox.cs.pdq.datasources.services;

import uk.ac.ox.cs.pdq.AccessException;


// TODO: Auto-generated Javadoc
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
