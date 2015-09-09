package uk.ac.ox.cs.pdq.endpoint.format;

import uk.ac.ox.cs.pdq.fol.Query;


/**
 * A factory for result formatters.
 * 
 * @author Julien LEBLAY
 */
public class ResultFormatterFactory {

	/**
	 * @param q
	 * @param name
	 * @return the appropriate implementation of a ResultFormatter depending on
	 * the given inputs.
	 */
	public static ResultFormatter getFormatter(Query<?> q, String name) {
		return new HtmlTabularResultFormatter();
	}
}
