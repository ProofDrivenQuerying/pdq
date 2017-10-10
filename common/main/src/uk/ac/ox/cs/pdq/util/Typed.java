package uk.ac.ox.cs.pdq.util;

import java.lang.reflect.Type;

/**
 * Common interface for typed objects, such as attributes and constants.
 *
 * @author Julien Leblay
 */
public interface Typed {
	
	/**
	 * Gets the type.
	 *
	 * @return the underlying of the type of this object. Not to be confused
	 * with the object's class.
	 */
	Type getType();
}
