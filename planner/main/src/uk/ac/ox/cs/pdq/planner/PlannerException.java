package uk.ac.ox.cs.pdq.planner;

/**
 * Exception that occurred during a planning operation.
 *
 * @author Julien Leblay
 *
 */
public class PlannerException extends Exception {

	/** */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * Default constructor. No message or root cause.
	 */
	public PlannerException() {
		super();
	}

	/**
	 * @param msg exception message.
	 */
	public PlannerException(String msg) {
		super(msg);
	}

	/**
	 * @param msg exception's message
	 * @param cause root cause of the problem.
	 */
	public PlannerException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * @param cause root cause of the problem.
	 */
	public PlannerException(Throwable cause) {
		super(cause);
	}
}
