package uk.ac.ox.cs.pdq.db;

import java.lang.reflect.Type;

/**
 * A top level interface for data type (which includes entity relations). 
 * @author Julien Leblay
 */
public interface DataType extends Type {
	
	/**
	 * Gets the name of the data type.
	 *
	 * @return the name
	 */
	String getName();
	
	/**
	 * TOCOMMENT What is this?
	 * Checks if is assignable from.
	 *
	 * @param o the o
	 * @return true, if is assignable from
	 */
	boolean isAssignableFrom(Object o);
}