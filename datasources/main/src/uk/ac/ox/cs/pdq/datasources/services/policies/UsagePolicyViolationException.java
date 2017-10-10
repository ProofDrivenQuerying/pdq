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

	/** The Constant serialVersionUID. */
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
