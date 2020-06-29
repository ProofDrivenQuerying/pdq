// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation used to distinguish methods that can automatically be timed through
 * aspect oriented programming aspect oriented programming aspect oriented programming aspect oriented programming 
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
