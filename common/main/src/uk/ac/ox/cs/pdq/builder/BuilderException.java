package uk.ac.ox.cs.pdq.builder;

// TODO: Auto-generated Javadoc
/**
 * Exception that occurred during a building operation.
 *
 * @author Julien Leblay
 *
 */
public class BuilderException extends RuntimeException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * Default constructor. No message or root cause.
	 */
	public BuilderException() {
		super();
	}

	/**
	 * Instantiates a new builder exception.
	 *
	 * @param msg exception message.
	 */
	public BuilderException(String msg) {
		super(msg);
	}

	/**
	 * Instantiates a new builder exception.
	 *
	 * @param msg exception's message
	 * @param cause root cause of the problem.
	 */
	public BuilderException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
