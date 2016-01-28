package uk.ac.ox.cs.pdq.test.planner;

/**
 * Exception that occurred during a regression test.
 * 
 * @author Julien Leblay
 *
 */
public class RegressionTestException extends Exception {

	/** */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * Default constructor. No message or root cause.
	 */
	public RegressionTestException() {
		super();
	}

	/**
	 * @param msg exception message.
	 */
	public RegressionTestException(String msg) {
		super(msg);
	}

	/**
	 * @param msg exception's message
	 * @param cause root cause of the problem.
	 */
	public RegressionTestException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
