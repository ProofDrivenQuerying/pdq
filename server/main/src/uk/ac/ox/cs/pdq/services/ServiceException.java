package uk.ac.ox.cs.pdq.services;

// TODO: Auto-generated Javadoc
/**
 * Top-level exception for anything happening at the service level.
 * 
 * @author Julien Leblay
 */
public class ServiceException extends RuntimeException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -799260471165064397L;

	/**
	 * Constructor for ServiceException.
	 * @param message String
	 * @param cause Throwable
	 * @param enableSuppression boolean
	 * @param writableStackTrace boolean
	 */
	public ServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * Constructor for ServiceException.
	 * @param message String
	 * @param cause Throwable
	 */
	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for ServiceException.
	 * @param message String
	 */
	public ServiceException(String message) {
		super(message);
	}

	/**
	 * Constructor for ServiceException.
	 * @param cause Throwable
	 */
	public ServiceException(Throwable cause) {
		super(cause);
	}
}
