package uk.ac.ox.cs.pdq.rewrite;

// TODO: Auto-generated Javadoc
/**
 * Exception that occurred during a rewriting operation.
 *
 * @author Julien Leblay
 *
 */
public class RewriterException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * Default constructor. No message or root cause.
	 */
	public RewriterException() {
		super();
	}

	/**
	 * Instantiates a new rewriter exception.
	 *
	 * @param msg exception message.
	 */
	public RewriterException(String msg) {
		super(msg);
	}

	/**
	 * Instantiates a new rewriter exception.
	 *
	 * @param msg exception's message
	 * @param cause root cause of the problem.
	 */
	public RewriterException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
