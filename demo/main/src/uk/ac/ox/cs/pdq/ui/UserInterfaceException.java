package uk.ac.ox.cs.pdq.ui;

/**
 * Exception throws in the UI level 
 * @author Julien Leblay
 */
public class UserInterfaceException extends RuntimeException {

	/** */
	private static final long serialVersionUID = -9013444660000182476L;

	/**
	 * Default constructor
	 * @param msg
	 */
    public UserInterfaceException(String msg) {
        super(msg);
    }

	/**
	 * 
	 * @param e
	 */
    public UserInterfaceException(Throwable e) {
        super(e);
    }

	/**
	 * 
	 * @param msg
	 * @param e
	 */
    public UserInterfaceException(String msg, Throwable e) {
        super(msg, e);
    }
}
