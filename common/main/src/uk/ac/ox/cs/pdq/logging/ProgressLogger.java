package uk.ac.ox.cs.pdq.logging;

// TODO: Auto-generated Javadoc
/**
 * Common interface for ad-hoc loggers, i.e. performance and progress loggers.
 * The functionalities provided by the interface are disjointed from Log4j.
 *
 * @author Julien Leblay
 */
public interface ProgressLogger extends AutoCloseable {

	/**
	 * Log.
	 */
	void log();

	/**
	 * Log.
	 *
	 * @param suffix String
	 */
	void log(String suffix);
}
