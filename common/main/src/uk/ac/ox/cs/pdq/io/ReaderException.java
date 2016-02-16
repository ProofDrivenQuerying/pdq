package uk.ac.ox.cs.pdq.io;


// TODO: Auto-generated Javadoc
/**
 * Exception that occurred during read operations.
 *
 * @author Julien Leblay
 */
public class ReaderException extends RuntimeException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * Default constructor. No message or root cause.
	 */
	public ReaderException() {
		super();
	}

	/**
	 * Instantiates a new reader exception.
	 *
	 * @param msg exception message.
	 */
	public ReaderException(String msg) {
		super(msg);
	}

	/**
	 * Instantiates a new reader exception.
	 *
	 * @param msg exception's message
	 * @param cause root cause of the problem.
	 */
	public ReaderException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
