package uk.ac.ox.cs.pdq.io;

import java.io.InputStream;


// TODO: Auto-generated Javadoc
/**
 * Reads experiment sample elements from XML.
 *
 * @author Julien Leblay
 * @param <T> the generic type
 */
public interface Reader<T> {

	/**
	 * Reads and returns an object as read from the given input.
	 *
	 * @param in the in
	 * @return a instance of Object, properly-typed, as read from the given
	 * input.
	 */
	T read(InputStream in);
}
