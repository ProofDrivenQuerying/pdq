package uk.ac.ox.cs.pdq.services;

/**
 * Event for single access to an online service.
 * 
 * @author Julien Leblay
 *
 */
public interface AccessEvent {

	/**
	 * 
	 * @return true if the event carries usage violation information
	 */
	boolean hasUsageViolationMessage();

	/**
	 * Sets the event's usage violation information
	 * @param msg String
	 */
	void setUsageViolationMessage(String msg);

	/**
	 * 
	 * @return the event's usage violation information if any, null otherwise
	 */
	String getUsageViolationMessage();
}
