// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources;

/**
 * Access exception implementation .
 *
 * @author Julien Leblay
 * 
 */
public class AccessException extends RuntimeException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -315472460756924167L;


	/**
	 * Default constructor. No message or root cause.
	 */
	public AccessException() {
		super();
	}

	/**
	 *
	 * @param msg exception's message
	 */
	public AccessException(String msg) {
		super(msg);
	}

	/**
	 *
	 * @param msg exception's message
	 * @param cause root cause of the problem.
	 */
	public AccessException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
