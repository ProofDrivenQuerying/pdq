package uk.ac.ox.cs.pdq.db;

import java.lang.reflect.Type;

// TODO: Auto-generated Javadoc
/**
 * A top level interface for data type (which includes entity relations). 
 * @author Julien Leblay
 */
public interface DataType extends Type {
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	String getName();
	
	/**
	 * Checks if is assignable from.
	 *
	 * @param o the o
	 * @return true, if is assignable from
	 */
	boolean isAssignableFrom(Object o);
}
