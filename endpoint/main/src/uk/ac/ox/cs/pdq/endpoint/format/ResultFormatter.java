package uk.ac.ox.cs.pdq.endpoint.format;

import java.io.PrintWriter;

import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;

/**
 * This interface encompasses formatter for query results.
 * 
 * @author Julien LEBLAY
 */
public interface ResultFormatter {

	/**
	 * Writes the output of the given operator to the given print writer.
	 * @param op
	 * @param buffer
	 * @throws XRException
	 */
	public <T> void process(TupleIterator it, PrintWriter out);
}
