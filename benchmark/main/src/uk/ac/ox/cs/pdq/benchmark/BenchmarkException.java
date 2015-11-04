package uk.ac.ox.cs.pdq.benchmark;

/**
 * Exception that occurred during a planning operation.
 * 
 * @author Julien Leblay
 *
 */
public class BenchmarkException extends Exception {

	/** */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * Default constructor. No message or root cause.
	 */
	public BenchmarkException() {
		super();
	}

	/**
	 * @param msg exception message.
	 */
	public BenchmarkException(String msg) {
		super(msg);
	}

	/**
	 * @param msg exception's message
	 * @param cause root cause of the problem.
	 */
	public BenchmarkException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
