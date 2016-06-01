package uk.ac.ox.cs.pdq.services;

import java.io.PrintStream;

/**
 * Top-level interface for all services. A service can typically be started, 
 * stopped, and asked for its name or status.
 * 
 * @author Julien Leblay
 *
 */
public interface Service extends Runnable {
	/**
	 * The service's name.
	 * @return String
	 */
	String getName();
	
	/**
	 * Print the service's current status to the given PrintStream.
	 * @param out PrintStream
	 */
	void status(PrintStream out);

	/**
	 *  Stops the service.
	 */
	void stop();
}
