package uk.ac.ox.cs.pdq.runtime;

// TODO: Auto-generated Javadoc
/**
 * Exception that occurred during an evaluation (of plan or query).
 * 
 * @author Julien Leblay
 */
public class EvaluationException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * Default constructor. No message or root cause.
	 */
	public EvaluationException() {
		super();
	}

	/**
	 * Instantiates a new evaluation exception.
	 *
	 * @param msg exception message.
	 */
	public EvaluationException(String msg) {
		super(msg);
	}

	/**
	 * TOCOMMENT: DIFFERENTIATE CONSTRUCTORS
	 *
	 * @param cause Throwable
	 */
	public EvaluationException(Throwable cause) {
		super(cause);
	}

	/**
	 * TOCOMMENT: DIFFERENTIATE CONSTRUCTORS
	 *
	 * @param msg exception's message
	 * @param cause root cause of the problem.
	 */
	public EvaluationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
