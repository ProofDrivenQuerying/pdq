package uk.ac.ox.cs.pdq.services;



/**
 * An access's response event
 * @author Julien Leblay
 */
public interface ResponseEvent extends AccessEvent {

	/**
	 * 
	 * @return true if the request must be followed by at least one other for
	 * the access to be complete.
	 */
	boolean hasMoreRequestEvents();

	/**
	 * 
	 * @return the next RequestEvent that should follow this ResponseEvent if
	 * hasMoreElevent return true. Otherwise, throws a NoSuchElementException.
	 */
	RequestEvent nextRequestEvent();
}
