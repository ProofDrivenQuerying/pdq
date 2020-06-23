// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.reasoning.chase;

/**
 * Exception that occurred during chasing.
 *
 * @author Efthymia Tsamoura
 */
public class ChaseException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * Default constructor. No message or root cause.
	 */
	public ChaseException() {
		super();
	}

	/**
	 * Instantiates a new chase exception.
	 *
	 * @param msg exception message.
	 */
	public ChaseException(String msg) {
		super(msg);
	}

	/**
	 * Instantiates a new chase exception.
	 *
	 * @param msg exception's message
	 * @param cause root cause of the problem.
	 */
	public ChaseException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Instantiates a new chase exception.
	 *
	 * @param cause root cause of the problem.
	 */
	public ChaseException(Throwable cause) {
		super(cause);
	}
}
