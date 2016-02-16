package uk.ac.ox.cs.pdq.endpoint.util;

import java.io.Serializable;

import uk.ac.ox.cs.pdq.logging.ProgressLogger;

// TODO: Auto-generated Javadoc
/**
 * The Interface BufferedProgressLogger.
 *
 * @author Julien Leblay
 */
public interface BufferedProgressLogger extends ProgressLogger, Serializable {
	
	/**
	 * Gets the log.
	 *
	 * @return the log
	 */
	public String getLog();
}
