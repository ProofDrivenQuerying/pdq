package uk.ac.ox.cs.pdq.data.sql;

public class DatabaseException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public DatabaseException(String msg) {
		super(msg);
	}
	
	public DatabaseException(String msg, Throwable cause) {
		super(msg,cause);
	}

}
