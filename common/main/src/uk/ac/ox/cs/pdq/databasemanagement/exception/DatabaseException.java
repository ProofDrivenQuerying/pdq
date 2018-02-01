package uk.ac.ox.cs.pdq.databasemanagement.exception;

/**
 * This exception class is used for any kind of error or exception in the
 * database manager. Have no specific functions, exists only to have a specific
 * exception name instead of using the superclass directly.
 * 
 * @author Gabor
 *
 */
public class DatabaseException extends Exception {
	private static final long serialVersionUID = 1L;

	public DatabaseException() {
	}

	public DatabaseException(String message) {
		super(message);
	}

	public DatabaseException(Throwable cause) {
		super(cause);
	}

	public DatabaseException(String message, Throwable cause) {
		super(message, cause);
	}

	public DatabaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
