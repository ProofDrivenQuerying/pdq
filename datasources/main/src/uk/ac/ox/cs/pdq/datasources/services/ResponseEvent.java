// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.services;



/**
 * An access's response event.
 *
 * @author Julien Leblay
 */
public interface ResponseEvent extends AccessEvent {

	/**
	 * Checks for more request events.
	 *
	 * @return true if the request must be followed by at least one other for
	 * the access to be complete.
	 */
	boolean hasMoreRequestEvents();

	/**
	 * Next request event.
	 *
	 * @return the next RequestEvent that should follow this ResponseEvent if
	 * hasMoreElevent return true. Otherwise, throws a NoSuchElementException.
	 */
	RequestEvent nextRequestEvent();
}
