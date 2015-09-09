package uk.ac.ox.cs.pdq;

/**
 * 
 * Access exception implementation 
 *
 * @author Julien Leblay
 */
public class AccessException extends RuntimeException {

	/** */
	private static final long serialVersionUID = -315472460756924167L;


	/**
	 * Default constructor. No message or root cause.
	 */
	public AccessException() {
		super();
	}

	/**
	 * @param msg exception's message
	 */
	public AccessException(String msg) {
		super(msg);
	}

	/**
	 * @param msg exception's message
	 * @param cause root cause of the problem.
	 */
	public AccessException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
