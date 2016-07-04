package uk.ac.ox.cs.pdq.util;

/**
 * TOCOMMENT This seems redundant. Also, a term has a name and Skolem, variable could inherit from there.
 * Common interface for named objects, such as variable and Skolems.
 *
 * @author Julien Leblay
 */
public interface Named {
	
	/**
	 * Gets the name.
	 *
	 * @return the name of this object.
	 */
	String getName();
}
