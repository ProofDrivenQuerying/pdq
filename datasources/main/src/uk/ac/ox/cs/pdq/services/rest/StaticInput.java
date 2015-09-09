package uk.ac.ox.cs.pdq.services.rest;

/**
 * A static input
 * 
 * @author Julien Leblay
 *
 * @param <T>
 */
public interface StaticInput<T> {
	/**
	 * @return The default value of the static input attribute
	 */
	T getDefaultValue();
}
