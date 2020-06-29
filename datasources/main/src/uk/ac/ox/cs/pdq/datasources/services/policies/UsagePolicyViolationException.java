// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.services.policies;

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

	public UsagePolicyViolationException() {
		super();
	}

	public UsagePolicyViolationException(String msg) {
		super(msg);
	}

	public UsagePolicyViolationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
