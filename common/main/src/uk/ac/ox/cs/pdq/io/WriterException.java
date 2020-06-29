// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io;

/**
 * Exception that occurs during write operations.
 *
 * @author Julien Leblay
 */
public class WriterException extends RuntimeException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * Default constructor. No message or root cause.
	 */
	public WriterException() {
		super();
	}

	/**
	 * Instantiates a new writer exception.
	 *
	 * @param msg exception message.
	 */
	public WriterException(String msg) {
		super(msg);
	}

	/**
	 * Instantiates a new writer exception.
	 *
	 * @param msg exception's message
	 * @param cause root cause of the problem.
	 */
	public WriterException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
