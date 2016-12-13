package uk.ac.ox.cs.pdq.algebra2;

/**
 * Top-level exception for logical operator-related exceptions.

 * @author Julien Leblay
 */
public class RelationalOperatorException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8211019926393887496L;

	/**
	 * Default constructor.
	 *
	 */
	public RelationalOperatorException() {
		super();
	}

	/**
	 * Default constructor.
	 *
	 * @param msg
	 *            the message associated with this exception.
	 */
	public RelationalOperatorException(String msg) {
		super(msg);
	}

	/**
	 * Default constructor.
	 *
	 * @param msg
	 *            the message associated with this exception.
	 * @param cause
	 *            the Throwable that caused this exception.
	 */
	public RelationalOperatorException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
