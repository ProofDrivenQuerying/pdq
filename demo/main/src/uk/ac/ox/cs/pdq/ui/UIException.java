package uk.ac.ox.cs.pdq.ui;

// TODO: Auto-generated Javadoc
/**
 * Exception that occurred during a planning operation.
 * 
 * @author Julien Leblay
 *
 */
public class UIException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * Default constructor. No message or root cause.
	 */
	public UIException() {
		super();
	}

	/**
	 * Instantiates a new UI exception.
	 *
	 * @param msg exception message.
	 */
	public UIException(String msg) {
		super(msg);
	}

	/**
	 * Instantiates a new UI exception.
	 *
	 * @param msg exception's message
	 * @param cause root cause of the problem.
	 */
	public UIException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
