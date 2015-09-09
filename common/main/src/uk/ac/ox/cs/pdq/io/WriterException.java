package uk.ac.ox.cs.pdq.io;

/**
 * Exception that occurred during write operations.
 *
 * @author Julien Leblay
 *
 */
public class WriterException extends RuntimeException {

	/** */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * Default constructor. No message or root cause.
	 */
	public WriterException() {
		super();
	}

	/**
	 * @param msg exception message.
	 */
	public WriterException(String msg) {
		super(msg);
	}

	/**
	 * @param msg exception's message
	 * @param cause root cause of the problem.
	 */
	public WriterException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
