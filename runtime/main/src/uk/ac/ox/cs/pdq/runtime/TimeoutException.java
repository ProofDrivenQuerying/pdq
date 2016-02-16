package uk.ac.ox.cs.pdq.runtime;


// TODO: Auto-generated Javadoc
/**
 * Exception that occurred when an evaluation time out is reached.
 * 
 * @author Julien Leblay
 */
public class TimeoutException extends EvaluationException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * Default constructor. No message or root cause.
	 */
	public TimeoutException() {
		super();
	}

	/**
	 * Instantiates a new timeout exception.
	 *
	 * @param msg exception message.
	 */
	public TimeoutException(String msg) {
		super(msg);
	}

	/**
	 * Instantiates a new timeout exception.
	 *
	 * @param cause Throwable
	 */
	public TimeoutException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new timeout exception.
	 *
	 * @param msg exception's message
	 * @param cause root cause of the problem.
	 */
	public TimeoutException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
