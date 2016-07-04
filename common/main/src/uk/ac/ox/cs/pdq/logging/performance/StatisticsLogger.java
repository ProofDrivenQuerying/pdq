package uk.ac.ox.cs.pdq.logging.performance;

/**
 * Top-level class for all statistics logger.
 *
 * @author Julien Leblay
 */
public abstract class StatisticsLogger {

	/** The field separator. */
	public static Character FIELD_SEPARATOR = '\t';

	/**
	 * TOCOMMENT ???
	 * 
	 * Make line.
	 *
	 * @return String
	 */
	protected abstract String makeLine();

	/**
	 * TOCOMMENT ???
	 * 
	 * Make header.
	 *
	 * @return String
	 */
	protected abstract String makeHeader();
}
