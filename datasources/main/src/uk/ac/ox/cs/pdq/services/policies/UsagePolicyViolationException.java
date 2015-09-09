package uk.ac.ox.cs.pdq.services.policies;

import uk.ac.ox.cs.pdq.AccessException;



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
