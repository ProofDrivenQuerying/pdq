// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

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
