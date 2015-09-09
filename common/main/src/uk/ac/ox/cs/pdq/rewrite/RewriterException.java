package uk.ac.ox.cs.pdq.rewrite;

/**
 * Exception that occurred during a rewriting operation.
 *
 * @author Julien Leblay
 *
 */
public class RewriterException extends Exception {

	/** */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * Default constructor. No message or root cause.
	 */
	public RewriterException() {
		super();
	}

	/**
	 * @param msg exception message.
	 */
	public RewriterException(String msg) {
		super(msg);
	}

	/**
	 * @param msg exception's message
	 * @param cause root cause of the problem.
	 */
	public RewriterException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
