package uk.ac.ox.cs.pdq.reasoning.chase;

/**
 * Exception that occurred during chasing
 *
 * @author Efthymia Tsamoura
 *
 */
public class ChaseException extends Exception {

	/** */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * Default constructor. No message or root cause.
	 */
	public ChaseException() {
		super();
	}

	/**
	 * @param msg exception message.
	 */
	public ChaseException(String msg) {
		super(msg);
	}

	/**
	 * @param msg exception's message
	 * @param cause root cause of the problem.
	 */
	public ChaseException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * @param cause root cause of the problem.
	 */
	public ChaseException(Throwable cause) {
		super(cause);
	}
}
