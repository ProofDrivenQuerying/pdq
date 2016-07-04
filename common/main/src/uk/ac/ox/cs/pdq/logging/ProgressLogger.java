package uk.ac.ox.cs.pdq.logging;

/**
 * Common interface for ad-hoc loggers, i.e. performance and progress loggers.
 * The functionalities provided by the interface are disjointed from Log4j.
 *
 * @author Julien Leblay
 */
public interface ProgressLogger extends AutoCloseable {


	void log();


	void log(String suffix);
}
