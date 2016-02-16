package uk.ac.ox.cs.pdq.test;

// TODO: Auto-generated Javadoc
/**
 * Exception that occurred during a regression test.
 * 
 * @author Julien Leblay
 *
 */
public class RegressionTestException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * Default constructor. No message or root cause.
	 */
	public RegressionTestException() {
		super();
	}

	/**
	 * Instantiates a new regression test exception.
	 *
	 * @param msg exception message.
	 */
	public RegressionTestException(String msg) {
		super(msg);
	}

	/**
	 * Instantiates a new regression test exception.
	 *
	 * @param msg exception's message
	 * @param cause root cause of the problem.
	 */
	public RegressionTestException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
