// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.logging;

/**
 * Top-level class for all statistics logger.
 *
 * @author Julien Leblay
 */
public abstract class StatisticsLogger {

	/** The field separator. */
	public static Character FIELD_SEPARATOR = '\t';

	/**
	 * Make line.
	 *
	 * @return String
	 */
	protected abstract String makeLine();

	/**
	 * Make header.
	 *
	 * @return String
	 */
	protected abstract String makeHeader();
}
