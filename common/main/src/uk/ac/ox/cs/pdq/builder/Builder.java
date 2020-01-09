package uk.ac.ox.cs.pdq.builder;

/**
 * Interface common to builder class.
 * Builder are typically use to instantiate objects that are too complex to
 * initialize with a single constructor calls, e.g. if many fields are
 * mandatory and many consistency checks are required.
 * The builder class works as a proxy, receiving all necessary initializations
 * on behalf to the object to be created.
 * The final object is actually instantiated upon a call to the build() method.
 *
 * @author Julien Leblay
 *
 * @param <T> the type of objects built
 */
public interface Builder<T> {

	/**
	 *
	 * @return a newly instantiated object.
	 */
	T build();
}
