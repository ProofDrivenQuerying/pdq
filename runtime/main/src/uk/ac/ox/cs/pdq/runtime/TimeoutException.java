package uk.ac.ox.cs.pdq.runtime;

/**
 * Exception that occurred when an evaluation time out is reached.
 * 
 * @author Julien Leblay
 */
public class TimeoutException extends EvaluationException {

	/** */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * Default constructor. No message or root cause.
	 */
	public TimeoutException() {
		super();
	}

	/**
	 * @param msg exception message.
	 */
	public TimeoutException(String msg) {
		super(msg);
	}

	/**
	 * @param cause Throwable
	 */
	public TimeoutException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param msg exception's message
	 * @param cause root cause of the problem.
	 */
	public TimeoutException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
