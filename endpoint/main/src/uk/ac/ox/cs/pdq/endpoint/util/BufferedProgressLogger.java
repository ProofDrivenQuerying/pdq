package uk.ac.ox.cs.pdq.endpoint.util;

import java.io.Serializable;

import uk.ac.ox.cs.pdq.logging.ProgressLogger;

/**
 * @author Julien Leblay
 *
 */
public interface BufferedProgressLogger extends ProgressLogger, Serializable {
	public String getLog();
}
