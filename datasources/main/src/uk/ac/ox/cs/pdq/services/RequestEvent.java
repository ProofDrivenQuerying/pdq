package uk.ac.ox.cs.pdq.services;

import uk.ac.ox.cs.pdq.AccessException;


/**
 * An access's request event
 * @author Julien Leblay
 *
 */
public interface RequestEvent extends AccessEvent {
	
	/**
	 * @return ResponseEvent
	 */
	ResponseEvent processRequest() throws AccessException;
}
