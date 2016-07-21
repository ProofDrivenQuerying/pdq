package uk.ac.ox.cs.pdq.db.homomorphism;

// TODO: Auto-generated Javadoc
/**
 * Exception that occurred during homomorphism detection.
 *
 * @author Efthymia Tsamoura
 *
 */
public class HomomorphismException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * Default constructor. No message or root cause.
	 */
	public HomomorphismException() {
		super();
	}

	/**
	 * Instantiates a new homomorphism exception.
	 *
	 * @param msg exception message.
	 */
	public HomomorphismException(String msg) {
		super(msg);
	}

	/**
	 * Instantiates a new homomorphism exception.
	 *
	 * @param msg exception's message
	 * @param cause root cause of the problem.
	 */
	public HomomorphismException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Instantiates a new homomorphism exception.
	 *
	 * @param cause root cause of the problem.
	 */
	public HomomorphismException(Throwable cause) {
		super(cause);
	}
}
