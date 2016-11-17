package uk.ac.ox.cs.pdq.endpoint.format;

import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;



// TODO: Auto-generated Javadoc
/**
 * A factory for result formatters.
 * 
 * @author Julien LEBLAY
 */
public class ResultFormatterFactory {

	/**
	 * Gets the formatter.
	 *
	 * @param q the q
	 * @param name the name
	 * @return the appropriate implementation of a ResultFormatter depending on
	 * the given inputs.
	 */
	public static ResultFormatter getFormatter(ConjunctiveQuery q, String name) {
		return new HtmlTabularResultFormatter();
	}
}
