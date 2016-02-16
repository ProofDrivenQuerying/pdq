package uk.ac.ox.cs.pdq;

// TODO: Auto-generated Javadoc
/**
 * Exception that occurred when an evaluation time out is reached.
 *
 * @author Julien Leblay
 */
public class LimitReachedException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * The Enum Reasons.
	 */
	public static enum Reasons {
/** The timeout. */
TIMEOUT, 
 /** The max iteration. */
 MAX_ITERATION}

	/** The reason. */
	private final Reasons reason;

	/**
	 * Instantiates a new limit reached exception.
	 *
	 * @param msg exception message.
	 * @param reason Reasons
	 */
	public LimitReachedException(String msg, Reasons reason) {
		super(msg);
		this.reason = reason;
	}

	/**
	 * Instantiates a new limit reached exception.
	 *
	 * @param reason Reasons
	 */
	public LimitReachedException(Reasons reason) {
		super();
		this.reason = reason;
	}

	/**
	 * Gets the reason.
	 *
	 * @return Reasons
	 */
	public Reasons getReason() {
		return this.reason;
	}
}
