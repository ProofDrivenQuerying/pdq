// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.legacy.services.policies;

import uk.ac.ox.cs.pdq.datasources.AccessException;



/**
 * Exception thrown when a usage policy is violated. Detailed on the violation
 * should be provided in the message.
 * 
 * @author Julien Leblay
 * 
 */
public class UsagePolicyViolationException extends AccessException {

	private static final long serialVersionUID = -8611823447617411625L;

	/**
	 * Instantiates a new usage policy violation exception.
	 */
	public UsagePolicyViolationException() {
		super();
	}

	/**
	 * Constructor for UsagePolicyViolationException.
	 * @param msg String
	 */
	public UsagePolicyViolationException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for UsagePolicyViolationException.
	 * @param msg String
	 * @param cause Throwable
	 */
	public UsagePolicyViolationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
