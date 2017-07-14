package uk.ac.ox.cs.pdq.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation used to distinguish method that can automatically be timed through
 * AOP.
 *
 * @author Julien Leblay
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Timed {
	/**
	 * The stats under which to log the collection runtime.
	 * @return StatKeys
	 */
	String key();
}
