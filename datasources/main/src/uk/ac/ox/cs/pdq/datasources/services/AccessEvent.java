package uk.ac.ox.cs.pdq.datasources.services;

// TODO: Auto-generated Javadoc
/**
 * Event for single access to an online service.
 * 
 * @author Julien Leblay
 *
 */
public interface AccessEvent {

	/**
	 * Checks for usage violation message.
	 *
	 * @return true if the event carries usage violation information
	 */
	boolean hasUsageViolationMessage();

	/**
	 * Sets the event's usage violation information.
	 *
	 * @param msg String
	 */
	void setUsageViolationMessage(String msg);

	/**
	 * Gets the usage violation message.
	 *
	 * @return the event's usage violation information if any, null otherwise
	 */
	String getUsageViolationMessage();
}
