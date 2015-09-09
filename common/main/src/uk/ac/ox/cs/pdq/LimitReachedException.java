package uk.ac.ox.cs.pdq;

/**
 * Exception that occurred when an evaluation time out is reached.
 *
 * @author Julien Leblay
 */
public class LimitReachedException extends Exception {

	/** */
	private static final long serialVersionUID = -315472460756924167L;

	/** */
	public static enum Reasons {TIMEOUT, MAX_ITERATION}

	private final Reasons reason;

	/**
	 * @param msg exception message.
	 * @param reason Reasons
	 */
	public LimitReachedException(String msg, Reasons reason) {
		super(msg);
		this.reason = reason;
	}

	/**
	 * @param reason Reasons
	 */
	public LimitReachedException(Reasons reason) {
		super();
		this.reason = reason;
	}

	/**
	 * @return Reasons
	 */
	public Reasons getReason() {
		return this.reason;
	}
}
