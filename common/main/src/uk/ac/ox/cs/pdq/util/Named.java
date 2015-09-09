package uk.ac.ox.cs.pdq.util;

/**
 * Common interface for named objects, such as variable and Skolems.
 *
 * @author Julien Leblay
 */
public interface Named {
	/**
	 * @return the name of this object.
	 */
	String getName();
}
