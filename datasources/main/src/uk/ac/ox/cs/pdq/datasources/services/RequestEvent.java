// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.services;

import uk.ac.ox.cs.pdq.datasources.AccessException;
import uk.ac.ox.cs.pdq.datasources.services.AccessEvent;


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
