// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.legacy.services.rest;

/**
 *
 * @author Julien Leblay
 * @param <T> the generic type
 */
public interface StaticInput<T> {
	
	/**
	 *
	 * @return The default value of the static input attribute
	 */
	T getDefaultValue();
}
