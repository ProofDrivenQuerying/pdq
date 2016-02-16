package uk.ac.ox.cs.pdq.planner;

// TODO: Auto-generated Javadoc
/**
 * Exception that occurred during a planning operation.
 *
 * @author Julien Leblay
 *
 */
public class PlannerException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * Default constructor. No message or root cause.
	 */
	public PlannerException() {
		super();
	}

	/**
	 * Instantiates a new planner exception.
	 *
	 * @param msg exception message.
	 */
	public PlannerException(String msg) {
		super(msg);
	}

	/**
	 * Instantiates a new planner exception.
	 *
	 * @param msg exception's message
	 * @param cause root cause of the problem.
	 */
	public PlannerException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Instantiates a new planner exception.
	 *
	 * @param cause root cause of the problem.
	 */
	public PlannerException(Throwable cause) {
		super(cause);
	}
}
