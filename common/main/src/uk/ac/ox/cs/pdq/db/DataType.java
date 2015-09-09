package uk.ac.ox.cs.pdq.db;

import java.lang.reflect.Type;

/**
 * A top level interface for data type (which includes entity relations). 
 * @author Julien Leblay
 */
public interface DataType extends Type {
	String getName();
	boolean isAssignableFrom(Object o);
}
