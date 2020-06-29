// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

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
