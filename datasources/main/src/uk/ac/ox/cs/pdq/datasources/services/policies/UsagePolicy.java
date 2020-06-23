// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.services.policies;




/**
 * Usage policy for web resources. Implemented this class to model ad-hoc usage
 * policy that web services put in places (e.g. data allowance, bandwidth
 * limits, authentication, etc.)
 * 
 * @author Julien Leblay
 * 
 */
public interface UsagePolicy {

	UsagePolicy copy();
}
