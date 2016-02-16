package uk.ac.ox.cs.pdq.ui;

// TODO: Auto-generated Javadoc
/**
 * Exception throws in the UI level .
 *
 * @author Julien Leblay
 */
public class UserInterfaceException extends RuntimeException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -9013444660000182476L;

	/**
	 * Default constructor.
	 *
	 * @param msg the msg
	 */
    public UserInterfaceException(String msg) {
        super(msg);
    }

	/**
	 * Instantiates a new user interface exception.
	 *
	 * @param e the e
	 */
    public UserInterfaceException(Throwable e) {
        super(e);
    }

	/**
	 * Instantiates a new user interface exception.
	 *
	 * @param msg the msg
	 * @param e the e
	 */
    public UserInterfaceException(String msg, Throwable e) {
        super(msg, e);
    }
}
