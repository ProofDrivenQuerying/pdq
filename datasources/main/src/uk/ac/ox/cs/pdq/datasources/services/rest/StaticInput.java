package uk.ac.ox.cs.pdq.datasources.services.rest;

/**
 * A static input.
 *
 * @author Julien Leblay
 * @param <T> the generic type
 */
public interface StaticInput<T> {
	
	/**
	 * Gets the default value.
	 *
	 * @return The default value of the static input attribute
	 */
	T getDefaultValue();
}
