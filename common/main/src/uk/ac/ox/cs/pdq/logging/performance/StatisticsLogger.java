package uk.ac.ox.cs.pdq.logging.performance;

/**
 * Top-level class for all statistics logger.
 *
 * @author Julien Leblay
 */
public abstract class StatisticsLogger {

	public static Character FIELD_SEPARATOR = '\t';

	/**
	 * @return String
	 */
	protected abstract String makeLine();

	/**
	 * @return String
	 */
	protected abstract String makeHeader();
}
