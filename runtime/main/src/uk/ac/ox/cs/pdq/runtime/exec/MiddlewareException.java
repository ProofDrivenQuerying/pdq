package uk.ac.ox.cs.pdq.runtime.exec;

import uk.ac.ox.cs.pdq.runtime.EvaluationException;

/**
 * Exception that occurred in the middleware.
 * 
 * @author Julien Leblay
 */
public class MiddlewareException extends EvaluationException {

	/** */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * Default constructor. No message or root cause.
	 */
	public MiddlewareException() {
		super();
	}

	/**
	 * @param msg exception message.
	 */
	public MiddlewareException(String msg) {
		super(msg);
	}

	/**
	 * @param cause the root cause.
	 */
	public MiddlewareException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param msg exception's message
	 * @param cause root cause of the problem.
	 */
	public MiddlewareException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
