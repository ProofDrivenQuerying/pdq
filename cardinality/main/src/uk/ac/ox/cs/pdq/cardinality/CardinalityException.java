/*
 * 
 */
package uk.ac.ox.cs.pdq.cardinality;

// TODO: Auto-generated Javadoc
/**
 * Exception that occurred during a planning operation.
 *
 * @author Julien Leblay
 *
 */
public class CardinalityException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * Default constructor. No message or root cause.
	 */
	public CardinalityException() {
		super();
	}

	/**
	 * Instantiates a new planner exception.
	 *
	 * @param msg exception message.
	 */
	public CardinalityException(String msg) {
		super(msg);
	}

	/**
	 * Instantiates a new planner exception.
	 *
	 * @param msg exception's message
	 * @param cause root cause of the problem.
	 */
	public CardinalityException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Instantiates a new planner exception.
	 *
	 * @param cause root cause of the problem.
	 */
	public CardinalityException(Throwable cause) {
		super(cause);
	}
}
