package uk.ac.ox.cs.pdq.reasoning.homomorphism;

/**
 * Exception that occurred during homomorphism detection.
 *
 * @author Efthymia Tsamoura
 *
 */
public class HomomorphismException extends Exception {

	/** */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * Default constructor. No message or root cause.
	 */
	public HomomorphismException() {
		super();
	}

	/**
	 * @param msg exception message.
	 */
	public HomomorphismException(String msg) {
		super(msg);
	}

	/**
	 * @param msg exception's message
	 * @param cause root cause of the problem.
	 */
	public HomomorphismException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * @param cause root cause of the problem.
	 */
	public HomomorphismException(Throwable cause) {
		super(cause);
	}
}
